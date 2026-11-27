"use client";

import { useCallback, useEffect, useState } from "react";

type Member = { id: string; userId: string; percentage: number };
type Group = { id: string; name: string; ownerUserId: string | null; members: Member[]; canEdit: boolean };
type Share = { memberUserId: string; allocatedAmount: number; status: "PENDING" | "REPORTED_PAID"; paidAt: string | null; reportedBy: string | null };
type History = { transactionId: string; description: string; totalAmount: number; createdAt: string; shares: Share[] };
const currency = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`/api/proxy/api/${path}`, { ...init, cache: "no-store" });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || `Serviço indisponível (${response.status}).`);
  }
  const body = await response.text();
  return body ? JSON.parse(body) as T : undefined as T;
}

export default function SplitGroupsPanel({ refreshKey = 0 }: { refreshKey?: number }) {
  const [groups, setGroups] = useState<Group[]>([]);
  const [selected, setSelected] = useState("");
  const [history, setHistory] = useState<{ groupId: string; items: History[] } | null>(null);
  const [editing, setEditing] = useState(false);
  const [editName, setEditName] = useState("");
  const [editMembers, setEditMembers] = useState<Member[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  const refreshGroups = useCallback(async () => {
    const items = await api<Group[]>("split-groups");
    setGroups(items);
    setSelected((current) => items.some((group) => group.id === current) ? current : items[0]?.id ?? "");
  }, []);

  const refreshHistory = useCallback(async (groupId: string) => {
    if (!groupId) {
      setHistory(null);
      return;
    }
    const items = await api<History[]>(`split-groups/${groupId}/history`);
    setHistory({ groupId, items });
  }, []);

  useEffect(() => {
    let active = true;
    api<Group[]>("split-groups").then((items) => {
      if (!active) return;
      setGroups(items);
      setSelected((current) => items.some((group) => group.id === current) ? current : items[0]?.id ?? "");
    }).catch((caught: unknown) => {
      if (active) setError(caught instanceof Error ? caught.message : "Não foi possível carregar os rateios.");
    });
    return () => { active = false; };
  }, [refreshKey]);

  useEffect(() => {
    if (!selected) return;
    let active = true;
    api<History[]>(`split-groups/${selected}/history`).then((items) => {
      if (active) setHistory({ groupId: selected, items });
    }).catch((caught: unknown) => {
      if (active) setError(caught instanceof Error ? caught.message : "Não foi possível carregar o histórico.");
    });
    return () => { active = false; };
  }, [refreshKey, selected]);

  const current = groups.find((group) => group.id === selected);
  const currentHistory = history?.groupId === selected ? history.items : [];
  const fieldClass = "bg-[#18181b] border border-white/10 rounded-xl px-3 py-2 text-xs";

  function startEdit(group: Group) {
    setEditName(group.name);
    setEditMembers(group.members.map((member) => ({ ...member })));
    setEditing(true);
    setError(null);
  }

  async function saveEdit(event: React.FormEvent) {
    event.preventDefault();
    if (!current) return;
    setLoading(true);
    setError(null);
    try {
      await api(`split-groups/${current.id}`, {
        method: "PUT", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name: editName, members: editMembers.map(({ userId, percentage }) => ({ userId, percentage: Number(percentage) })) }),
      });
      setEditing(false);
      await refreshGroups();
      await refreshHistory(current.id);
      setNotice("Grupo de rateio atualizado.");
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Não foi possível editar o grupo.");
    } finally {
      setLoading(false);
    }
  }

  async function markPaid(item: History, memberId?: string) {
    setLoading(true);
    setError(null);
    setNotice(null);
    try {
      const path = memberId
        ? `split-transactions/${item.transactionId}/shares/${memberId}/report-paid`
        : `split-transactions/${item.transactionId}/report-my-payment`;
      await api(path, { method: "PATCH" });
      await refreshHistory(selected);
      setNotice("Pagamento registrado como declaração manual. Nenhum dinheiro foi transferido.");
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Não foi possível registrar a declaração.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="bg-[#121214] border border-white/10 rounded-3xl p-6 space-y-4">
      <div>
        <h2 className="text-sm font-bold">Rateios e participantes</h2>
        <p className="mt-2 text-xs text-amber-300">As cotas marcadas como pagas são declarações manuais dos participantes, não comprovam liquidação nem movimentam dinheiro.</p>
        <p className="mt-1 text-xs text-zinc-500">Convites por e-mail/SMS ficam indisponíveis até esses canais serem configurados.</p>
      </div>
      {error && <p role="alert" className="text-xs text-red-400">{error}</p>}
      {notice && <p role="status" className="text-xs text-emerald-400">{notice}</p>}
      {groups.length === 0 ? <p className="text-xs text-zinc-500">Você ainda não participa de grupos de rateio.</p> : <>
        <label htmlFor="split-group-select" className="block text-xs text-zinc-400">Grupo</label>
        <select id="split-group-select" value={selected} onChange={(event) => { setSelected(event.target.value); setEditing(false); }} className={`w-full ${fieldClass}`}>
          {groups.map((group) => <option key={group.id} value={group.id}>{group.name}</option>)}
        </select>
        {current && <div className="space-y-3">
          <p className="break-all text-[10px] text-zinc-500">ID: {current.id}</p>
          {current.canEdit && !editing && <button type="button" onClick={() => startEdit(current)} className="text-xs text-[#00FF66] underline">Editar grupo</button>}
          {editing && current.canEdit && <form onSubmit={saveEdit} className="space-y-2">
            <label htmlFor="split-edit-name" className="block text-xs text-zinc-400">Nome do grupo</label>
            <input id="split-edit-name" value={editName} onChange={(event) => setEditName(event.target.value)} maxLength={80} required className={`w-full ${fieldClass}`} />
            {editMembers.map((member, index) => <div key={member.id} className="grid grid-cols-[1fr_5rem] gap-2">
              <span className="truncate self-center text-[10px] text-zinc-400">{member.userId}</span>
              <input aria-label={`Cota do participante ${index + 1}`} type="number" min="0.01" max="100" step="0.01" value={member.percentage} onChange={(event) => setEditMembers((members) => members.map((item, position) => position === index ? { ...item, percentage: Number(event.target.value) } : item))} className={fieldClass} />
            </div>)}
            <p className="text-xs text-zinc-500">A soma das cotas deve continuar em 100%. Convites externos não serão enviados.</p>
            <div className="flex gap-2">
              <button type="submit" disabled={loading} className="flex-1 rounded-xl bg-[#00FF66] py-2 text-xs font-bold text-black disabled:opacity-50">Salvar alterações</button>
              <button type="button" onClick={() => setEditing(false)} className="flex-1 rounded-xl bg-white/10 py-2 text-xs">Cancelar</button>
            </div>
          </form>}
          {!editing && <div className="flex flex-wrap gap-2">{current.members.map((member) => <span key={member.id} className="rounded-full bg-white/5 px-2 py-1 text-[10px] text-zinc-400">{member.userId.slice(0, 8)}… · {member.percentage}%</span>)}</div>}
          <h3 className="pt-2 text-sm font-semibold">Histórico de despesas</h3>
          {currentHistory.length === 0 ? <p className="text-xs text-zinc-500">Nenhuma despesa no grupo.</p> : currentHistory.map((item) => <article key={item.transactionId} className="space-y-2 border-t border-white/5 pt-3">
            <div className="flex justify-between gap-2 text-xs"><span>{item.description}</span><span>{currency.format(item.totalAmount)}</span></div>
            <p className="text-[10px] text-zinc-500">{new Date(item.createdAt).toLocaleString("pt-BR")}</p>
            {item.shares.map((share) => <div key={share.memberUserId} className="flex items-center justify-between gap-2 text-[10px]">
              <span className="truncate">{share.memberUserId.slice(0, 8)}… · {currency.format(share.allocatedAmount)} · {share.status === "REPORTED_PAID" ? "declarado pago" : "pendente"}</span>
              {current.canEdit && share.status === "PENDING" && <button type="button" disabled={loading} onClick={() => void markPaid(item, share.memberUserId)} className="text-[#00FF66] underline disabled:opacity-50">Registrar cota</button>}
            </div>)}
            {item.shares.some((share) => share.status === "PENDING") && <button type="button" disabled={loading} onClick={() => void markPaid(item)} className="text-xs text-amber-300 underline disabled:opacity-50">Reportar minha cota como paga</button>}
          </article>)}
          <button type="button" disabled className="w-full cursor-not-allowed rounded-xl border border-white/10 py-2 text-xs text-zinc-500">Convidar por e-mail/SMS (canal não configurado)</button>
        </div>}
      </>}
    </section>
  );
}
