"use client";

import { useCallback, useEffect, useRef, useState } from "react";

type Account = { balance: number };
type Transaction = { id: string; type: "CREDIT" | "PIX" | "TRANSFER" | "BILL"; amount: number; description: string; createdAt: string };
type Schedule = { id: string; amount: number; destination: string; scheduledAt: string; status: "PENDING" | "COMPLETED" | "FAILED" | "CANCELLED" };
type Bill = { id: string; description: string; amount: number; dueDate: string; status: "PENDING" | "PAID" };
type PendingOperation = { signature: string; requestId: string };
const currency = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
const scheduleStatus: Record<Schedule["status"], string> = {
  PENDING: "Pendente", COMPLETED: "Concluído", FAILED: "Falhou: saldo fictício insuficiente", CANCELLED: "Cancelado",
};

async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`/api/proxy/simulation/${path}`, { ...init, cache: "no-store" });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || `Serviço indisponível (${response.status}).`);
  }
  return response.json();
}

export default function SimulationPanel() {
  const [available, setAvailable] = useState<boolean | null>(null);
  const [account, setAccount] = useState<Account | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [bills, setBills] = useState<Bill[]>([]);
  const [amount, setAmount] = useState("");
  const [destination, setDestination] = useState("");
  const [scheduledAt, setScheduledAt] = useState("");
  const [billDescription, setBillDescription] = useState("");
  const [billAmount, setBillAmount] = useState("");
  const [billDueDate, setBillDueDate] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const pending = useRef<PendingOperation | null>(null);

  const refresh = useCallback(async () => {
    const response = await fetch("/api/proxy/simulation/account", { cache: "no-store" });
    if (response.status === 404) {
      setAccount(null);
      setTransactions([]);
      setSchedules([]);
      setBills([]);
      return;
    }
    if (!response.ok) {
      const body = await response.json().catch(() => null);
      throw new Error(body?.message || "Não foi possível consultar a conta simulada.");
    }
    const current: Account = await response.json();
    const [statement, upcoming, billList] = await Promise.all([api<Transaction[]>("statement"), api<Schedule[]>("schedules"), api<Bill[]>("bills")]);
    setAccount(current);
    setTransactions(statement);
    setSchedules(upcoming);
    setBills(billList);
  }, []);

  useEffect(() => {
    async function load() {
      try {
        const status = await fetch("/api/proxy/simulation/status", { cache: "no-store" });
        if (status.status === 404) {
          setAvailable(false);
          return;
        }
        if (!status.ok) throw new Error("Não foi possível verificar o ambiente simulado.");
        await refresh();
        setAvailable(true);
      } catch (caught) {
        setError(caught instanceof Error ? caught.message : "Falha ao carregar a simulação.");
      }
    }
    void load();
  }, [refresh]);

  async function createAccount() {
    setBusy(true);
    setError(null);
    try {
      await api<Account>("account", { method: "POST" });
      await refresh();
      setNotice("Conta simulada criada com saldo fictício zerado.");
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Falha ao criar a conta simulada.");
    } finally {
      setBusy(false);
    }
  }

  async function operate(type: "credit" | "pix" | "transfer" | "schedule") {
    setError(null);
    setNotice(null);
    const normalized = amount.trim().replace(",", ".");
    if (!/^\d{1,9}([.]\d{1,2})?$/.test(normalized) || Number(normalized) <= 0) {
      setError("Informe um valor positivo de até R$ 999.999.999,99, com no máximo duas casas decimais.");
      return;
    }
    const name = destination.trim();
    if (type !== "credit" && (!name || name.length > 100)) {
      setError("Informe um nome fictício de até 100 caracteres.");
      return;
    }
    const date = type === "schedule" ? new Date(scheduledAt) : null;
    if (type === "schedule" && (!date || Number.isNaN(date.getTime()) || date.getTime() <= Date.now())) {
      setError("Informe uma data e hora futuras para o Pix simulado.");
      return;
    }
    const operation = { amount: normalized, destination: type === "credit" ? null : name, scheduledAt: date?.toISOString() };
    const signature = JSON.stringify({ type, ...operation });
    if (pending.current?.signature !== signature) {
      pending.current = { signature, requestId: crypto.randomUUID() };
    }
    setBusy(true);
    try {
      await api(type === "schedule" ? "schedules" : type, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ requestId: pending.current.requestId, ...operation }),
      });
      await refresh();
      pending.current = null;
      setAmount("");
      setDestination("");
      setScheduledAt("");
      setNotice(type === "credit" ? "Saldo fictício adicionado." : type === "schedule" ? "Pix simulado agendado." : "Operação simulada registrada; nenhum dinheiro foi enviado.");
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Falha na operação simulada.");
    } finally {
      setBusy(false);
    }
  }

  async function cancelSchedule(id: string) {
    setBusy(true);
    setError(null);
    try {
      await api(`schedules/${id}/cancel`, { method: "POST" });
      await refresh();
      setNotice("Agendamento simulado cancelado.");
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Falha ao cancelar agendamento.");
    } finally {
      setBusy(false);
    }
  }

  async function createBill() {
    setError(null);
    setNotice(null);
    const normalized = billAmount.trim().replace(",", ".");
    const description = billDescription.trim();
    if (!/^\d{1,9}([.]\d{1,2})?$/.test(normalized) || Number(normalized) <= 0
        || !description || description.length > 100 || !billDueDate) {
      setError("Informe descrição fictícia, valor positivo e vencimento.");
      return;
    }
    const operation = { amount: normalized, description, dueDate: billDueDate };
    const signature = JSON.stringify({ type: "bill", ...operation });
    if (pending.current?.signature !== signature) pending.current = { signature, requestId: crypto.randomUUID() };
    setBusy(true);
    try {
      await api("bills", { method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ requestId: pending.current.requestId, ...operation }) });
      await refresh();
      pending.current = null;
      setBillDescription("");
      setBillAmount("");
      setBillDueDate("");
      setNotice("Boleto fictício cadastrado.");
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Falha ao cadastrar boleto fictício.");
    } finally {
      setBusy(false);
    }
  }

  async function payBill(id: string) {
    setBusy(true);
    setError(null);
    setNotice(null);
    try {
      await api(`bills/${id}/pay`, { method: "POST" });
      await refresh();
      setNotice("Pagamento simulado registrado; nenhum dinheiro foi enviado.");
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Falha ao pagar boleto fictício.");
    } finally {
      setBusy(false);
    }
  }

  if (available === false) return null;

  return (
    <section className="bg-[#121214] border border-amber-400/30 rounded-3xl p-6 space-y-4">
      <div>
        <h2 className="text-sm font-bold text-white">Ambiente simulado</h2>
        <p className="text-xs text-amber-300 mt-2">Saldo, Pix e transferências fictícios. Nenhum dinheiro é movimentado e nenhum destinatário recebe valores. Não informe chaves Pix ou dados reais.</p>
      </div>
      {error && <p role="alert" className="text-xs text-red-400">{error}</p>}
      {notice && <p role="status" className="text-xs text-emerald-400">{notice}</p>}
      {available === null ? (
        <p className="text-xs text-zinc-400">{error ? "Simulação indisponível." : "Carregando simulação..."}</p>
      ) : !account ? (
        <button type="button" disabled={busy} onClick={createAccount} className="w-full bg-[#00FF66] text-black py-3 rounded-2xl text-xs font-bold disabled:opacity-50">
          Criar conta simulada
        </button>
      ) : (
        <>
          <div>
            <p className="text-xs text-zinc-400">Saldo fictício</p>
            <p className="text-2xl font-bold">{currency.format(account.balance)}</p>
          </div>
          <form onSubmit={(event) => { event.preventDefault(); void operate("credit"); }} className="space-y-3">
            <label htmlFor="simulation-amount" className="block text-xs text-zinc-400">Valor fictício em reais</label>
            <input id="simulation-amount" value={amount} onChange={(event) => setAmount(event.target.value)} inputMode="decimal" maxLength={12} placeholder="0,00" className="w-full bg-[#18181b] border border-white/10 rounded-2xl px-4 py-3 text-sm" />
            <label htmlFor="simulation-destination" className="block text-xs text-zinc-400">Nome fictício do destinatário (Pix ou transferência)</label>
            <input id="simulation-destination" value={destination} onChange={(event) => setDestination(event.target.value)} maxLength={100} className="w-full bg-[#18181b] border border-white/10 rounded-2xl px-4 py-3 text-sm" />
            <label htmlFor="simulation-date" className="block text-xs text-zinc-400">Data e hora locais (somente para agendamento)</label>
            <input id="simulation-date" type="datetime-local" value={scheduledAt} onChange={(event) => setScheduledAt(event.target.value)} className="w-full bg-[#18181b] border border-white/10 rounded-2xl px-4 py-3 text-sm" />
            <div className="grid grid-cols-2 gap-3">
              <button type="submit" disabled={busy} className="bg-[#00FF66] text-black py-3 rounded-2xl text-xs font-bold disabled:opacity-50">Adicionar saldo fictício</button>
              <button type="button" disabled={busy} onClick={() => void operate("pix")} className="border border-[#00FF66]/30 text-[#00FF66] py-3 rounded-2xl text-xs font-bold disabled:opacity-50">Simular Pix</button>
            </div>
            <button type="button" disabled={busy} onClick={() => void operate("transfer")} className="w-full border border-white/20 py-3 rounded-2xl text-xs font-bold disabled:opacity-50">Simular transferência</button>
            <button type="button" disabled={busy} onClick={() => void operate("schedule")} className="w-full border border-white/20 py-3 rounded-2xl text-xs font-bold disabled:opacity-50">Agendar Pix simulado</button>
          </form>
          <div>
            <h3 className="text-sm font-bold">Extrato simulado</h3>
            <p className="text-xs text-zinc-500 mt-1">Últimas 50 operações fictícias.</p>
            {transactions.length === 0 ? <p className="text-xs text-zinc-500 mt-3">Nenhuma operação simulada.</p> : (
              <ul className="mt-3 space-y-3">
                {transactions.map((transaction) => (
                  <li key={transaction.id} className="flex justify-between gap-3 text-xs">
                    <span><span className="block text-zinc-200">{transaction.type === "CREDIT" ? transaction.description : `${transaction.type === "PIX" ? "Pix para" : transaction.type === "BILL" ? "Boleto fictício:" : "Transferência para"} ${transaction.description}`}</span><span className="text-zinc-500">{new Date(transaction.createdAt).toLocaleString("pt-BR")}</span><details className="mt-1 text-zinc-400"><summary>Comprovante simulado</summary><p>Nenhum dinheiro foi movimentado.</p><p>Identificador: {transaction.id}</p><p>Valor fictício: {currency.format(transaction.amount)}</p></details></span>
                    <span className={transaction.type !== "CREDIT" ? "text-red-400" : "text-emerald-400"}>{transaction.type !== "CREDIT" ? "-" : "+"}{currency.format(transaction.amount)}</span>
                  </li>
                ))}
              </ul>
            )}
          </div>
          <div>
            <h3 className="text-sm font-bold">Agendamentos simulados</h3>
            {schedules.length === 0 ? <p className="text-xs text-zinc-500 mt-3">Nenhum agendamento.</p> : (
              <ul className="mt-3 space-y-3">
                {schedules.map((schedule) => (
                  <li key={schedule.id} className="text-xs text-zinc-300">
                    <p>{schedule.destination} · {currency.format(schedule.amount)}</p>
                    <p className="text-zinc-500">{new Date(schedule.scheduledAt).toLocaleString("pt-BR")} · {scheduleStatus[schedule.status]}</p>
                    {schedule.status === "PENDING" && <button type="button" disabled={busy} onClick={() => void cancelSchedule(schedule.id)} className="mt-1 text-amber-300 underline disabled:opacity-50">Cancelar</button>}
                  </li>
                ))}
              </ul>
            )}
          </div>
          <div className="space-y-3">
            <h3 className="text-sm font-bold">Boletos simulados</h3>
            <p className="text-xs text-amber-300">Use somente descrições fictícias; não informe código de barras ou dados reais.</p>
            <label htmlFor="bill-description" className="block text-xs text-zinc-400">Descrição fictícia</label>
            <input id="bill-description" value={billDescription} onChange={(event) => setBillDescription(event.target.value)} maxLength={100} className="w-full bg-[#18181b] border border-white/10 rounded-2xl px-4 py-3 text-sm" />
            <label htmlFor="bill-amount" className="block text-xs text-zinc-400">Valor fictício em reais</label>
            <input id="bill-amount" value={billAmount} onChange={(event) => setBillAmount(event.target.value)} inputMode="decimal" maxLength={12} className="w-full bg-[#18181b] border border-white/10 rounded-2xl px-4 py-3 text-sm" />
            <label htmlFor="bill-due" className="block text-xs text-zinc-400">Vencimento fictício</label>
            <input id="bill-due" type="date" value={billDueDate} onChange={(event) => setBillDueDate(event.target.value)} className="w-full bg-[#18181b] border border-white/10 rounded-2xl px-4 py-3 text-sm" />
            <button type="button" disabled={busy} onClick={() => void createBill()} className="w-full border border-white/20 py-3 rounded-2xl text-xs font-bold disabled:opacity-50">Cadastrar boleto fictício</button>
            {bills.length === 0 ? <p className="text-xs text-zinc-500">Nenhum boleto fictício.</p> : (
              <ul className="space-y-3">{bills.map((bill) => (
                <li key={bill.id} className="text-xs text-zinc-300">
                  <p>{bill.description} · {currency.format(bill.amount)}</p>
                  <p className="text-zinc-500">Vencimento: {bill.dueDate.split("-").reverse().join("/")} · {bill.status === "PAID" ? "Pago (simulado)" : "Pendente"}</p>
                  {bill.status === "PENDING" && <button type="button" disabled={busy} onClick={() => void payBill(bill.id)} className="mt-1 text-amber-300 underline disabled:opacity-50">Simular pagamento</button>}
                </li>
              ))}</ul>
            )}
          </div>
        </>
      )}
    </section>
  );
}
