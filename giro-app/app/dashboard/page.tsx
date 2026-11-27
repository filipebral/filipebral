"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import CreateSplitGroupModal from "@/components/CreateSplitGroupModal";
import ProcessSplitModal from "@/components/ProcessSplitModal";
import SimulationPanel from "@/components/SimulationPanel";
import FinancialSimulators from "@/components/FinancialSimulators";
import SimulationCardPanel from "@/components/SimulationCardPanel";
import FinancialOrganizationPanel from "@/components/FinancialOrganizationPanel";
import SplitGroupsPanel from "@/components/SplitGroupsPanel";
import ProviderReadinessPanel from "@/components/ProviderReadinessPanel";
import BusinessApplicationPanel from "@/components/BusinessApplicationPanel";

export default function Dashboard() {
  const router = useRouter();
  const [message, setMessage] = useState<string | null>(null);
  const [isGroupModalOpen, setIsGroupModalOpen] = useState(false);
  const [isProcessModalOpen, setIsProcessModalOpen] = useState(false);
  const [groupRefreshKey, setGroupRefreshKey] = useState(0);
  const [simulationRefreshKey, setSimulationRefreshKey] = useState(0);

  const handleLogout = async () => {
    await fetch("/api/auth/logout", { method: "POST" });
    router.replace("/");
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
            <span className="text-xs text-zinc-400 bg-white/5 px-2.5 py-1 rounded-full border border-white/10 font-semibold">Integração pendente</span>
          </div>
          <p className="text-sm text-zinc-400">Conta e saldo reais indisponíveis. Nenhum provedor bancário está conectado.</p>
          {message && <p role="status" className="mb-3 text-xs text-emerald-400">{message}</p>}
          <button type="button" disabled className="mt-4 w-full cursor-not-allowed rounded-2xl border border-white/10 py-3.5 text-xs font-bold text-zinc-500">Abertura de conta real indisponível</button>
        </section>

        <SimulationPanel refreshKey={simulationRefreshKey} onChanged={() => setSimulationRefreshKey((key) => key + 1)} />
        <FinancialSimulators />
        <SimulationCardPanel refreshKey={simulationRefreshKey} onChanged={() => setSimulationRefreshKey((key) => key + 1)} />
        <FinancialOrganizationPanel refreshKey={simulationRefreshKey} onChanged={() => setSimulationRefreshKey((key) => key + 1)} />
        <SplitGroupsPanel refreshKey={groupRefreshKey} />
        <ProviderReadinessPanel />
        <BusinessApplicationPanel />

        <div className="grid grid-cols-2 gap-3">
          <button onClick={() => setIsGroupModalOpen(true)} className="bg-[#121214] border border-[#00FF66]/30 p-4 rounded-2xl text-sm font-bold text-[#00FF66]">Novo rateio</button>
          <button onClick={() => setIsProcessModalOpen(true)} className="bg-[#121214] border border-white/5 p-4 rounded-2xl text-sm font-semibold text-zinc-300">Ratear valor</button>
        </div>

        <section className="bg-[#121214] border border-white/10 rounded-3xl p-6">
          <h2 className="text-sm font-bold text-white mb-3">Histórico recente</h2>
          <p className="text-center py-6 text-zinc-500 text-xs">Nenhuma movimentação registrada no momento.</p>
        </section>
      </div>

      <CreateSplitGroupModal isOpen={isGroupModalOpen} onClose={() => setIsGroupModalOpen(false)} onSuccess={() => { setMessage("Grupo de rateio criado com sucesso."); setGroupRefreshKey((key) => key + 1); }} />
      <ProcessSplitModal isOpen={isProcessModalOpen} onClose={() => setIsProcessModalOpen(false)} onSuccess={() => setGroupRefreshKey((key) => key + 1)} />
    </main>
  );
}
