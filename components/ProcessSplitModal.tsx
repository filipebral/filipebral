"use client";

import { useState } from "react";

interface MemberShare {
  userId: string;
  percentage: number;
  allocatedAmount: number;
}

interface SplitResult {
  transactionId: string;
  description: string;
  totalAmount: number;
  shares: MemberShare[];
}

interface ProcessSplitModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function ProcessSplitModal({ isOpen, onClose }: ProcessSplitModalProps) {
  const [splitGroupId, setSplitGroupId] = useState("");
  const [totalAmount, setTotalAmount] = useState("");
  const [description, setDescription] = useState("");
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<SplitResult | null>(null);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setResult(null);
    setLoading(true);

    try {
      const response = await fetch("/api/proxy/api/split-transactions", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          splitGroupId,
          totalAmount: parseFloat(totalAmount),
          description
        })
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || "Erro ao processar rateio.");
      }

      setResult(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Erro de comunicação com o servidor.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-md p-4 animate-fadeIn">
      <div className="bg-[#121214] border border-white/10 w-full max-w-lg rounded-3xl shadow-2xl overflow-hidden text-zinc-100">
        
        {/* Header */}
        <div className="px-8 pt-6 pb-4 flex justify-between items-center border-b border-white/5">
          <div>
            <span className="text-xs font-semibold uppercase tracking-wider text-purple-400">Motor de Transações</span>
            <h2 className="text-xl font-bold tracking-tight text-white">Processar Rateio</h2>
          </div>
          <button 
            onClick={onClose} 
            className="w-9 h-9 rounded-full bg-white/5 hover:bg-white/10 flex items-center justify-center text-zinc-400 hover:text-white transition-colors"
          >
            ✕
          </button>
        </div>

        {/* Form Body */}
        <div className="p-8 space-y-6 max-h-[80vh] overflow-y-auto custom-scrollbar">
          {error && (
            <div className="p-4 bg-red-500/10 border border-red-500/20 rounded-2xl text-red-400 text-sm flex items-center gap-3">
              <span className="w-2 h-2 rounded-full bg-red-400 animate-pulse"></span>
              {error}
            </div>
          )}

          {!result ? (
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-medium uppercase tracking-wider text-zinc-400 mb-1">ID do Grupo (UUID)</label>
                <input 
                  type="text" 
                  required
                  placeholder="Cole o ID do grupo criado..." 
                  value={splitGroupId}
                  onChange={(e) => setSplitGroupId(e.target.value)}
                  className="w-full bg-[#18181b] border border-white/10 rounded-2xl px-5 py-3.5 text-white placeholder-zinc-600 focus:outline-none focus:border-purple-500 text-xs font-mono"
                />
              </div>

              <div>
                <label className="block text-xs font-medium uppercase tracking-wider text-zinc-400 mb-1">Descrição da Despesa</label>
                <input 
                  type="text" 
                  required
                  placeholder="Ex: Jantar de Sexta, Aluguel..." 
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="w-full bg-[#18181b] border border-white/10 rounded-2xl px-5 py-3.5 text-white placeholder-zinc-600 focus:outline-none focus:border-purple-500 text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-medium uppercase tracking-wider text-zinc-400 mb-1">Valor Total (R$)</label>
                <input 
                  type="number" 
                  step="0.01"
                  min="0.01"
                  required
                  placeholder="0.00" 
                  value={totalAmount}
                  onChange={(e) => setTotalAmount(e.target.value)}
                  className="w-full bg-[#18181b] border border-white/10 rounded-2xl px-5 py-3.5 text-white placeholder-zinc-600 focus:outline-none focus:border-purple-500 text-lg font-bold"
                />
              </div>

              <div className="pt-4 border-t border-white/5 flex gap-3">
                <button 
                  type="button" 
                  onClick={onClose}
                  className="flex-1 px-5 py-3.5 rounded-2xl text-xs font-semibold text-zinc-400 hover:bg-white/5 transition-colors"
                >
                  Cancelar
                </button>
                <button 
                  type="submit" 
                  disabled={loading}
                  className="flex-1 px-5 py-3.5 rounded-2xl text-xs font-bold bg-[#820AD1] hover:bg-[#6c08ad] text-white shadow-lg shadow-purple-900/30 transition-all disabled:opacity-50"
                >
                  {loading ? "Calculando..." : "Calcular Rateio"}
                </button>
              </div>
            </form>
          ) : (
            <div className="space-y-4">
              <div className="p-4 bg-emerald-500/10 border border-emerald-500/20 rounded-2xl">
                <span className="text-xs text-emerald-400 font-semibold block uppercase">Rateio Calculado com Sucesso</span>
                <p className="text-white text-sm font-bold mt-1">{result.description}</p>
                <p className="text-emerald-400 text-lg font-extrabold mt-0.5">R$ {Number(result.totalAmount).toFixed(2)}</p>
              </div>

              <div className="space-y-2">
                <span className="text-xs font-medium uppercase tracking-wider text-zinc-400">Divisão por Participante:</span>
                <div className="space-y-2 max-h-48 overflow-y-auto pr-1">
                  {result.shares.map((share, idx) => (
                    <div key={idx} className="bg-[#18181b] p-3 rounded-2xl border border-white/5 flex justify-between items-center text-xs">
                      <div>
                        <span className="text-zinc-400 font-mono block text-[10px]">ID: {share.userId}</span>
                        <span className="text-zinc-200 font-semibold">Cota: {share.percentage}%</span>
                      </div>
                      <span className="text-purple-400 font-bold text-sm">R$ {Number(share.allocatedAmount).toFixed(2)}</span>
                    </div>
                  ))}
                </div>
              </div>

              <button 
                onClick={() => setResult(null)}
                className="w-full mt-4 bg-white/10 hover:bg-white/20 text-white py-3 rounded-2xl text-xs font-semibold transition-colors"
              >
                Novo Cálculo
              </button>
            </div>
          )}

        </div>

      </div>
    </div>
  );
}