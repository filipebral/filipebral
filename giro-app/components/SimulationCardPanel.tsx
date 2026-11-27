"use client";

import { useCallback, useEffect, useRef, useState } from "react";

type Card = { exists: boolean; creditLimit: number | null; blocked: boolean | null; availableLimit: number | null };
type Statement = { month: string; amount: number; status: "OPEN" | "PAID" };
type CardTransaction = { id: string; kind: "CREDIT" | "DEBIT"; description: string; amount: number; statementMonth: string | null; createdAt: string };
const currency = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`/api/proxy/simulation/${path}`, { ...init, cache: "no-store" });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || `Serviço indisponível (${response.status}).`);
  }
  return response.json();
}

async function loadCard() {
  const card = await request<Card>("card");
  if (!card.exists) return { card, statements: [] as Statement[], transactions: [] as CardTransaction[] };
  const [statements, transactions] = await Promise.all([
    request<Statement[]>("card/statements"),
    request<CardTransaction[]>("card/transactions"),
  ]);
  return { card, statements, transactions };
}

export default function SimulationCardPanel({ refreshKey, onChanged }: { refreshKey: number; onChanged: () => void }) {
  const [card, setCard] = useState<Card | null>(null);
  const [statements, setStatements] = useState<Statement[]>([]);
  const [transactions, setTransactions] = useState<CardTransaction[]>([]);
  const [limit, setLimit] = useState("");
  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  const [kind, setKind] = useState<"CREDIT" | "DEBIT">("CREDIT");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const pending = useRef(new Map<string, string>());

  const applyCard = useCallback((data: Awaited<ReturnType<typeof loadCard>>) => {
    setCard(data.card);
    setStatements(data.statements);
    setTransactions(data.transactions);
  }, []);

  const refresh = useCallback(async () => applyCard(await loadCard()), [applyCard]);

  useEffect(() => {
    let active = true;
    loadCard().then((data) => {
      if (active) applyCard(data);
    }).catch((caught: unknown) => {
      if (active) setError(caught instanceof Error ? caught.message : "Não foi possível carregar o cartão simulado.");
    });
    return () => { active = false; };
  }, [applyCard, refreshKey]);

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
      setError(caught instanceof Error ? caught.message : "Não foi possível concluir a operação simulada.");
      return false;
    } finally {
      setBusy(false);
    }
  }

  async function issue() {
    const normalized = limit.trim().replace(",", ".");
    if (!/^\d{1,9}([.]\d{1,2})?$/.test(normalized) || Number(normalized) <= 0) {
      setError("Informe um limite fictício positivo com até duas casas decimais.");
      return;
    }
    await run(() => request("card", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ creditLimit: normalized }),
    }), "Cartão virtual fictício configurado. Nenhum cartão real foi emitido.");
  }

  async function purchase(event: React.FormEvent) {
    event.preventDefault();
    const normalized = amount.trim().replace(",", ".");
    const merchant = description.trim();
    if (!/^\d{1,9}([.]\d{1,2})?$/.test(normalized) || Number(normalized) <= 0 || !merchant || merchant.length > 100) {
      setError("Informe valor válido e descrição fictícia da compra.");
      return;
    }
    const signature = JSON.stringify(["purchase", kind, normalized, merchant]);
    if (!pending.current.has(signature)) pending.current.set(signature, crypto.randomUUID());
    const completed = await run(() => request("card/purchases", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ requestId: pending.current.get(signature), kind, amount: normalized, description: merchant }),
    }), "Compra simulada registrada; nenhum estabelecimento recebeu pagamento.");
    if (completed) {
      pending.current.delete(signature);
      setAmount("");
      setDescription("");
    }
  }

  async function pay(statement: Statement) {
    const signature = `statement:${statement.month}`;
    if (!pending.current.has(signature)) pending.current.set(signature, crypto.randomUUID());
    const completed = await run(() => request(`card/statements/${statement.month}/pay`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ requestId: pending.current.get(signature) }),
    }), "Fatura simulada paga com saldo fictício.");
    if (completed) pending.current.delete(signature);
  }

  return (
    <section className="bg-[#121214] border border-amber-400/30 rounded-3xl p-6 space-y-4">
      <div>
        <h2 className="text-sm font-bold">Cartões simulados</h2>
        <p className="text-xs text-amber-300 mt-2">Não existe cartão emitido. Não há número, validade ou CVV e nenhuma compra real é possível.</p>
      </div>
      {error && <p role="alert" className="text-xs text-red-400">{error}</p>}
      {notice && <p role="status" className="text-xs text-emerald-400">{notice}</p>}
      {card === null ? <p className="text-xs text-zinc-400">Carregando cartão simulado...</p> : !card.exists ? (
        <div className="space-y-3">
          <label htmlFor="simulation-card-limit" className="block text-xs text-zinc-400">Limite de crédito fictício em reais</label>
          <input id="simulation-card-limit" value={limit} onChange={(event) => setLimit(event.target.value)} inputMode="decimal" maxLength={12} className="w-full bg-[#18181b] border border-white/10 rounded-2xl px-4 py-3 text-sm" />
          <button type="button" disabled={busy} onClick={() => void issue()} className="w-full bg-[#00FF66] text-black py-3 rounded-2xl text-xs font-bold disabled:opacity-50">Configurar cartão virtual fictício</button>
          <p className="text-xs text-zinc-500">Débito simulado exige conta fictícia criada no painel da conta.</p>
        </div>
      ) : (
        <>
          <div className="rounded-2xl border border-white/10 bg-[#18181b] p-4">
            <p className="text-xs text-zinc-400">Cartão virtual fictício · sem dados de pagamento</p>
            <p className="mt-1 text-[10px] text-zinc-500">Cartão físico e emissão de cartão real indisponíveis até integração aprovada.</p>
            <p className="mt-2 text-sm">{card.blocked ? "Bloqueado" : "Desbloqueado"}</p>
            <p className="mt-2 text-xs text-zinc-400">Limite: {currency.format(card.creditLimit ?? 0)} · Disponível: {currency.format(card.availableLimit ?? 0)}</p>
          </div>
          <button type="button" disabled={busy} onClick={() => void run(
            () => request(`card/${card.blocked ? "unblock" : "block"}`, { method: "POST" }),
            card.blocked ? "Cartão simulado desbloqueado." : "Cartão simulado bloqueado."
          )} className="w-full border border-white/20 py-3 rounded-2xl text-xs font-bold disabled:opacity-50">
            {card.blocked ? "Desbloquear cartão simulado" : "Bloquear cartão simulado"}
          </button>
          <div className="grid grid-cols-2 gap-2">
            <button type="button" disabled={busy || card.blocked === true} onClick={() => void run(
              () => request("card/report-loss", { method: "POST" }), "Relato fictício de perda registrado; cartão simulado bloqueado."
            )} className="rounded-xl border border-amber-400/30 py-2 text-xs text-amber-300 disabled:opacity-50">Relatar perda</button>
            <button type="button" disabled={busy || card.blocked === true} onClick={() => void run(
              () => request("card/report-fraud", { method: "POST" }), "Relato fictício de suspeita registrado; cartão simulado bloqueado."
            )} className="rounded-xl border border-amber-400/30 py-2 text-xs text-amber-300 disabled:opacity-50">Relatar suspeita</button>
          </div>
          <form onSubmit={purchase} className="space-y-3">
            <label htmlFor="simulation-card-kind" className="block text-xs text-zinc-400">Modalidade fictícia</label>
            <select id="simulation-card-kind" value={kind} onChange={(event) => setKind(event.target.value === "DEBIT" ? "DEBIT" : "CREDIT")} className="w-full bg-[#18181b] border border-white/10 rounded-2xl px-4 py-3 text-sm">
              <option value="CREDIT">Crédito — compõe fatura do mês</option>
              <option value="DEBIT">Débito — usa saldo fictício</option>
            </select>
            <label htmlFor="simulation-card-purchase" className="block text-xs text-zinc-400">Valor fictício da compra</label>
            <input id="simulation-card-purchase" value={amount} onChange={(event) => setAmount(event.target.value)} inputMode="decimal" maxLength={12} className="w-full bg-[#18181b] border border-white/10 rounded-2xl px-4 py-3 text-sm" />
            <label htmlFor="simulation-card-merchant" className="block text-xs text-zinc-400">Estabelecimento fictício</label>
            <input id="simulation-card-merchant" value={description} onChange={(event) => setDescription(event.target.value)} maxLength={100} className="w-full bg-[#18181b] border border-white/10 rounded-2xl px-4 py-3 text-sm" />
            <button type="submit" disabled={busy || card.blocked === true} className="w-full border border-[#00FF66]/30 text-[#00FF66] py-3 rounded-2xl text-xs font-bold disabled:opacity-50">Simular compra</button>
          </form>
          <div>
            <h3 className="text-sm font-bold">Faturas mensais fictícias</h3>
            {statements.length === 0 ? <p className="mt-2 text-xs text-zinc-500">Nenhuma compra no crédito simulada.</p> : (
              <ul className="mt-2 space-y-3">{statements.map((statement) => (
                <li key={statement.month} className="text-xs text-zinc-300">
                  <p>{statement.month} · {currency.format(statement.amount)} · {statement.status === "PAID" ? "Paga (simulação)" : "Em aberto"}</p>
                  {statement.status === "OPEN" && <button type="button" disabled={busy} onClick={() => void pay(statement)} className="mt-1 text-amber-300 underline disabled:opacity-50">Pagar com saldo fictício</button>}
                </li>
              ))}</ul>
            )}
          </div>
          <div>
            <h3 className="text-sm font-bold">Compras simuladas</h3>
            {transactions.length === 0 ? <p className="mt-2 text-xs text-zinc-500">Nenhuma compra registrada.</p> : (
              <ul className="mt-2 space-y-2">{transactions.map((transaction) => (
                <li key={transaction.id} className="flex justify-between gap-3 text-xs">
                  <span>{transaction.description}<span className="block text-zinc-500">{transaction.kind === "CREDIT" ? `Crédito · ${transaction.statementMonth}` : "Débito"} · {new Date(transaction.createdAt).toLocaleString("pt-BR")}</span></span>
                  <span>{currency.format(transaction.amount)}</span>
                </li>
              ))}</ul>
            )}
          </div>
        </>
      )}
    </section>
  );
}
