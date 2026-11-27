"use client";

import { useCallback, useEffect, useRef, useState } from "react";

type Account = { balance: number; reservedBalance: number; availableBalance: number };
type Category = { id: string; label: string };
type Budget = { month: string; category: string; label: string; limitAmount: number; spent: number; percent: number };
type Goal = { id: string; name: string; targetAmount: number; savedAmount: number; reservedBalance: number; availableBalance: number };
type Transaction = { id: string; type: string; amount: number; description: string; createdAt: string; category: string | null };
type CardTransaction = { id: string; kind: string; amount: number; description: string; createdAt: string; category: string | null };
type Notification = { id: string; type: string; message: string; createdAt: string; read: boolean };
type Channel = { channel: string; status: string };
const currency = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`/api/proxy/simulation/${path}`, { ...init, cache: "no-store" });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || `Serviço indisponível (${response.status}).`);
  }
  const body = await response.text();
  return body ? JSON.parse(body) as T : undefined as T;
}

function amountValue(value: string) {
  const normalized = value.trim().replace(",", ".");
  return /^\d{1,9}([.]\d{1,2})?$/.test(normalized) && Number(normalized) > 0 ? normalized : null;
}

async function loadOrganization(month: string) {
  const response = await fetch("/api/proxy/simulation/account", { cache: "no-store" });
  if (response.status === 404) return null;
  if (!response.ok) throw new Error("Não foi possível consultar a conta fictícia.");
  const account: Account = await response.json();
  const [categories, budgets, goals, transactions, cardTransactions, notifications, channels] = await Promise.all([
    api<Category[]>("finance/categories"),
    api<Budget[]>(`finance/budgets?month=${encodeURIComponent(month)}`),
    api<Goal[]>("finance/goals"),
    api<Transaction[]>("statement"),
    api<CardTransaction[]>("card/transactions"),
    api<Notification[]>("notifications"),
    api<Channel[]>("notifications/channels"),
  ]);
  return { account, categories, budgets, goals, transactions, cardTransactions, notifications, channels };
}

export default function FinancialOrganizationPanel({ refreshKey, onChanged }: { refreshKey: number; onChanged: () => void }) {
  const [month, setMonth] = useState(() => new Date().toISOString().slice(0, 7));
  const [account, setAccount] = useState<Account | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [budgets, setBudgets] = useState<Budget[]>([]);
  const [goals, setGoals] = useState<Goal[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [cardTransactions, setCardTransactions] = useState<CardTransaction[]>([]);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [channels, setChannels] = useState<Channel[]>([]);
  const [budgetCategory, setBudgetCategory] = useState("");
  const [budgetLimit, setBudgetLimit] = useState("");
  const [goalName, setGoalName] = useState("");
  const [goalTarget, setGoalTarget] = useState("");
  const [goalAmounts, setGoalAmounts] = useState<Record<string, string>>({});
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const pending = useRef(new Map<string, string>());

  const applyOrganization = useCallback((data: Awaited<ReturnType<typeof loadOrganization>>) => {
    if (!data) {
      setAccount(null);
      return;
    }
    setAccount(data.account);
    setCategories(data.categories);
    setBudgets(data.budgets);
    setGoals(data.goals);
    setTransactions(data.transactions);
    setCardTransactions(data.cardTransactions);
    setNotifications(data.notifications);
    setChannels(data.channels);
  }, []);

  const refresh = useCallback(async () => applyOrganization(await loadOrganization(month)), [applyOrganization, month]);

  useEffect(() => {
    let active = true;
    loadOrganization(month).then((data) => {
      if (active) applyOrganization(data);
    }).catch((caught: unknown) => {
      if (active) setError(caught instanceof Error ? caught.message : "Não foi possível carregar a organização financeira.");
    });
    return () => { active = false; };
  }, [applyOrganization, month, refreshKey]);

  async function run(action: () => Promise<unknown>, success: string) {
    setBusy(true);
    setError(null);
    setNotice(null);
    try {
      await action();
      onChanged();
      await refresh();
      setNotice(success);
      return true;
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Não foi possível concluir a operação.");
      return false;
    } finally {
      setBusy(false);
    }
  }

  async function saveBudget(event: React.FormEvent) {
    event.preventDefault();
    const limit = amountValue(budgetLimit);
    if (!budgetCategory || !limit) {
      setError("Selecione uma categoria e informe um limite positivo.");
      return;
    }
    const completed = await run(() => api("finance/budgets", {
      method: "PUT", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ month, category: budgetCategory, limitAmount: limit }),
    }), "Orçamento fictício salvo.");
    if (completed) setBudgetLimit("");
  }

  async function saveGoal(event: React.FormEvent) {
    event.preventDefault();
    const target = amountValue(goalTarget);
    if (!goalName.trim() || goalName.trim().length > 80 || !target) {
      setError("Informe nome da meta e valor-alvo positivo.");
      return;
    }
    const completed = await run(() => api("finance/goals", {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name: goalName.trim(), targetAmount: target }),
    }), "Meta fictícia criada.");
    if (completed) {
      setGoalName("");
      setGoalTarget("");
    }
  }

  async function moveGoal(goal: Goal, type: "contributions" | "releases") {
    const raw = amountValue(goalAmounts[goal.id] ?? "");
    if (!raw) {
      setError("Informe um valor positivo para a meta.");
      return;
    }
    const signature = `${goal.id}:${type}:${raw}`;
    if (!pending.current.has(signature)) pending.current.set(signature, crypto.randomUUID());
    const completed = await run(() => api(`finance/goals/${goal.id}/${type}`, {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ requestId: pending.current.get(signature), amount: raw }),
    }), type === "contributions" ? "Saldo fictício reservado para a meta." : "Saldo fictício liberado da meta.");
    if (completed) {
      pending.current.delete(signature);
      setGoalAmounts((current) => ({ ...current, [goal.id]: "" }));
    }
  }

  async function categorize(path: string, id: string, category: string) {
    await run(() => api(`${path}/${id}/category`, {
      method: "PATCH", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ category }),
    }), "Categoria fictícia atualizada.");
  }

  const fieldClass = "w-full bg-[#18181b] border border-white/10 rounded-2xl px-4 py-3 text-sm";
  return (
    <section className="bg-[#121214] border border-white/10 rounded-3xl p-6 space-y-5">
      <div>
        <h2 className="text-sm font-bold">Organização financeira simulada</h2>
        <p className="mt-2 text-xs text-amber-300">Categorias, orçamentos e metas usam apenas dados fictícios do Giro. As metas reservam saldo simulado; não há rendimento nem movimentação real.</p>
      </div>
      {error && <p role="alert" className="text-xs text-red-400">{error}</p>}
      {notice && <p role="status" className="text-xs text-emerald-400">{notice}</p>}
      {!account ? <p className="text-xs text-zinc-400">Crie primeiro a conta fictícia na seção “Ambiente simulado” para acompanhar orçamento e metas.</p> : (
        <>
          <div className="grid grid-cols-3 gap-2 text-xs">
            <p className="rounded-xl bg-[#18181b] p-3">Total<br /><strong>{currency.format(account.balance)}</strong></p>
            <p className="rounded-xl bg-[#18181b] p-3">Reservado<br /><strong>{currency.format(account.reservedBalance)}</strong></p>
            <p className="rounded-xl bg-[#18181b] p-3">Disponível<br /><strong>{currency.format(account.availableBalance)}</strong></p>
          </div>
          <div>
            <h3 className="text-sm font-semibold">Categorias das despesas fictícias</h3>
            <p className="my-2 text-xs text-zinc-500">A classificação atualiza os gráficos e orçamentos do mês.</p>
            {[...transactions.filter((transaction) => transaction.type !== "CREDIT" && transaction.type !== "CARD_BILL").map((transaction) => ({ ...transaction, source: "transactions" })),
              ...cardTransactions.filter((transaction) => transaction.kind === "CREDIT").map((transaction) => ({ ...transaction, type: "CREDIT" as const, source: "card-transactions" }))].map((expense) => (
              <div key={`${expense.source}:${expense.id}`} className="flex items-center justify-between gap-2 border-t border-white/5 py-2 text-xs">
                <span className="min-w-0 truncate">{expense.description} · {currency.format(expense.amount)}</span>
                <select aria-label={`Categoria de ${expense.description}`} value={expense.category ?? ""} onChange={(event) => void categorize(expense.source, expense.id, event.target.value)} className="max-w-32 rounded-lg bg-[#18181b] p-2">
                  <option value="" disabled>Categoria</option>
                  {categories.map((category) => <option key={category.id} value={category.id}>{category.label}</option>)}
                </select>
              </div>
            ))}
          </div>
          <div className="space-y-3">
            <h3 className="text-sm font-semibold">Orçamentos por categoria</h3>
            <label htmlFor="budget-month" className="block text-xs text-zinc-400">Mês</label>
            <input id="budget-month" type="month" value={month} onChange={(event) => setMonth(event.target.value)} className={fieldClass} />
            <form onSubmit={saveBudget} className="space-y-3">
              <label htmlFor="budget-category" className="block text-xs text-zinc-400">Categoria</label>
              <select id="budget-category" value={budgetCategory} onChange={(event) => setBudgetCategory(event.target.value)} className={fieldClass}>
                <option value="">Selecione</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.label}</option>)}
              </select>
              <label htmlFor="budget-limit" className="block text-xs text-zinc-400">Limite fictício em reais</label>
              <input id="budget-limit" value={budgetLimit} onChange={(event) => setBudgetLimit(event.target.value)} inputMode="decimal" maxLength={12} className={fieldClass} />
              <button type="submit" disabled={busy} className="w-full border border-white/20 py-3 rounded-2xl text-xs font-bold disabled:opacity-50">Salvar orçamento</button>
            </form>
            {budgets.map((budget) => {
              const percent = Math.min(100, Math.max(0, budget.percent));
              return <div key={`${budget.month}:${budget.category}`} className="space-y-1 text-xs">
                <div className="flex justify-between"><span>{budget.label}</span><span>{currency.format(budget.spent)} / {currency.format(budget.limitAmount)}</span></div>
                <div role="progressbar" aria-label={`Orçamento ${budget.label}`} aria-valuenow={Math.round(percent)} aria-valuemin={0} aria-valuemax={100} className="h-2 overflow-hidden rounded-full bg-white/10">
                  <div className={budget.percent >= 100 ? "h-full bg-red-400" : "h-full bg-[#00FF66]"} style={{ width: `${percent}%` }} />
                </div>
              </div>;
            })}
            {budgets.length === 0 && <p className="text-xs text-zinc-500">Nenhum orçamento neste mês.</p>}
          </div>
          <div className="space-y-3">
            <h3 className="text-sm font-semibold">Metas com saldo fictício reservado</h3>
            <form onSubmit={saveGoal} className="space-y-3">
              <label htmlFor="goal-name" className="block text-xs text-zinc-400">Nome da meta</label>
              <input id="goal-name" value={goalName} onChange={(event) => setGoalName(event.target.value)} maxLength={80} className={fieldClass} />
              <label htmlFor="goal-target" className="block text-xs text-zinc-400">Valor-alvo fictício em reais</label>
              <input id="goal-target" value={goalTarget} onChange={(event) => setGoalTarget(event.target.value)} inputMode="decimal" maxLength={12} className={fieldClass} />
              <button type="submit" disabled={busy} className="w-full border border-white/20 py-3 rounded-2xl text-xs font-bold disabled:opacity-50">Criar meta</button>
            </form>
            {goals.map((goal) => <div key={goal.id} className="space-y-2 border-t border-white/5 pt-3">
              <div className="flex justify-between text-xs"><strong>{goal.name}</strong><span>{currency.format(goal.savedAmount)} / {currency.format(goal.targetAmount)}</span></div>
              <div role="progressbar" aria-label={`Meta ${goal.name}`} aria-valuenow={Math.round(Math.min(100, goal.savedAmount / goal.targetAmount * 100))} aria-valuemin={0} aria-valuemax={100} className="h-2 overflow-hidden rounded-full bg-white/10">
                <div className="h-full bg-[#00FF66]" style={{ width: `${Math.min(100, goal.savedAmount / goal.targetAmount * 100)}%` }} />
              </div>
              <input aria-label={`Valor para ${goal.name}`} value={goalAmounts[goal.id] ?? ""} onChange={(event) => setGoalAmounts((current) => ({ ...current, [goal.id]: event.target.value }))} inputMode="decimal" maxLength={12} placeholder="Valor fictício" className={fieldClass} />
              <div className="grid grid-cols-2 gap-2">
                <button type="button" disabled={busy} onClick={() => void moveGoal(goal, "contributions")} className="rounded-xl border border-[#00FF66]/30 py-2 text-xs text-[#00FF66] disabled:opacity-50">Reservar saldo</button>
                <button type="button" disabled={busy || goal.savedAmount <= 0} onClick={() => void moveGoal(goal, "releases")} className="rounded-xl border border-white/20 py-2 text-xs disabled:opacity-50">Liberar saldo</button>
              </div>
            </div>)}
          </div>
          <div className="space-y-2">
            <h3 className="text-sm font-semibold">Notificações do aplicativo</h3>
            <p className="text-xs text-amber-300">E-mail, push e SMS não estão configurados; nenhum envio externo será alegado.</p>
            <div className="flex flex-wrap gap-2">{channels.map((channel) => <span key={channel.channel} className="rounded-full bg-white/5 px-2 py-1 text-[10px] text-zinc-400">{channel.channel}: não configurado</span>)}</div>
            {notifications.map((notification) => <div key={notification.id} className="flex justify-between gap-3 border-t border-white/5 py-2 text-xs">
              <span className={notification.read ? "text-zinc-500" : "text-zinc-200"}>{notification.message}<span className="block text-zinc-500">{new Date(notification.createdAt).toLocaleString("pt-BR")}</span></span>
              {!notification.read && <button type="button" disabled={busy} onClick={() => void run(
                () => api(`notifications/${notification.id}/read`, { method: "PATCH" }), "Notificação marcada como lida."
              )} className="text-[#00FF66] underline">Marcar lida</button>}
            </div>)}
            {notifications.length === 0 && <p className="text-xs text-zinc-500">Nenhuma notificação.</p>}
          </div>
        </>
      )}
    </section>
  );
}
