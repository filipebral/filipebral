"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import CreateSplitGroupModal from "@/components/CreateSplitGroupModal";
import ProcessSplitModal from "@/components/ProcessSplitModal";

export default function Dashboard() {
  const router = useRouter();
  const [customerId, setCustomerId] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isGroupModalOpen, setIsGroupModalOpen] = useState(false);
  const [isProcessModalOpen, setIsProcessModalOpen] = useState(false);

  const handleLogout = async () => {
    await fetch("/api/auth/logout", { method: "POST" });
    router.replace("/");
  };

  const handleCreateAccount = async () => {
    setLoading(true);
    setMessage(null);
    setError(null);
    try {
      const response = await fetch("/api/proxy/accounts", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ baasCustomerId: customerId }),
      });
      const result = await response.json();
      if (!response.ok) {
        throw new Error(result.message || "Não foi possível criar a conta bancária.");
      }
      setMessage("Conta bancária criada e vinculada ao Giro.");
    } catch (caughtError) {
      setError(caughtError instanceof Error ? caughtError.message : "Erro ao criar a conta.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen bg-[#09090b] text-zinc-100 flex flex-col items-center py-10 px-4 selection:bg-[#00FF66] selection:text-black">
      <div className="w-full max-w-md space-y-6">
        <div className="flex justify-between items-center px-2">
          <div>
            <span className="text-xs uppercase tracking-widest text-zinc-500 font-semibold">Conta Digital</span>
            <h1 className="text-xl font-bold text-white tracking-tight">Giro App</h1>
          </div>
          <button onClick={handleLogout} className="text-xs text-zinc-400 hover:text-white px-3 py-1.5 bg-white/5 rounded-xl border border-white/10">
            Sair
          </button>
        </div>

        <section className="bg-[#121214] border border-white/10 rounded-3xl p-6 shadow-2xl">
          <div className="flex justify-between items-center mb-4">
            <span className="text-xs font-medium text-zinc-400 uppercase tracking-wider">Saldo disponível</span>
            <span className="text-xs text-[#00FF66] bg-[#00FF66]/10 px-2.5 py-1 rounded-full border border-[#00FF66]/20 font-semibold">Ativo</span>
          </div>
          <div className="text-3xl font-extrabold text-white tracking-tight mb-6">R$ 0,00</div>
          <label htmlFor="customer-id" className="block text-xs text-zinc-400 mb-2">Identificador do cliente no BaaS</label>
          <input id="customer-id" value={customerId} onChange={(event) => setCustomerId(event.target.value)} className="w-full bg-[#18181b] border border-white/10 rounded-2xl px-4 py-3 text-white text-sm mb-3" />
          {message && <p role="status" className="mb-3 text-xs text-emerald-400">{message}</p>}
          {error && <p role="alert" className="mb-3 text-xs text-red-400">{error}</p>}
          <button onClick={handleCreateAccount} disabled={loading || !customerId.trim()} className="w-full bg-[#00FF66] hover:bg-[#00cc52] text-black py-3.5 px-4 rounded-2xl text-xs font-extrabold disabled:opacity-50">
            {loading ? "Processando..." : "Criar conta & vincular BaaS"}
          </button>
        </section>

        <div className="grid grid-cols-2 gap-3">
          <button onClick={() => setIsGroupModalOpen(true)} className="bg-[#121214] border border-[#00FF66]/30 p-4 rounded-2xl text-sm font-bold text-[#00FF66]">Novo rateio</button>
          <button onClick={() => setIsProcessModalOpen(true)} className="bg-[#121214] border border-white/5 p-4 rounded-2xl text-sm font-semibold text-zinc-300">Ratear valor</button>
        </div>

        <section className="bg-[#121214] border border-white/10 rounded-3xl p-6">
          <h2 className="text-sm font-bold text-white mb-3">Histórico recente</h2>
          <p className="text-center py-6 text-zinc-500 text-xs">Nenhuma movimentação registrada no momento.</p>
        </section>
      </div>

      <CreateSplitGroupModal isOpen={isGroupModalOpen} onClose={() => setIsGroupModalOpen(false)} onSuccess={() => setMessage("Grupo de rateio criado com sucesso.")} />
      <ProcessSplitModal isOpen={isProcessModalOpen} onClose={() => setIsProcessModalOpen(false)} />
    </main>
  );
}
