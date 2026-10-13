"use client";

import { useState } from "react";

interface GroupMemberInput {
  userId: string;
  percentage: number;
}

interface CreateSplitGroupModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export default function CreateSplitGroupModal({ isOpen, onClose, onSuccess }: CreateSplitGroupModalProps) {
  const [name, setName] = useState("");
  const [members, setMembers] = useState<GroupMemberInput[]>([
    { userId: "", percentage: 50 },
    { userId: "", percentage: 50 }
  ]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const totalPercentage = members.reduce((acc, curr) => acc + (Number(curr.percentage) || 0), 0);
  const isBalanced = Math.abs(totalPercentage - 100) < 0.01;

  const handleAddMember = () => {
    setMembers([...members, { userId: "", percentage: 0 }]);
  };

  const handleRemoveMember = (index: number) => {
    if (members.length <= 1) return;
    setMembers(members.filter((_, i) => i !== index));
  };

  const handleMemberChange = (index: number, field: keyof GroupMemberInput, value: string | number) => {
    const updated = [...members];
    updated[index] = { ...updated[index], [field]: value };
    setMembers(updated);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!isBalanced) {
      setError(`A soma das cotas deve totalizar exatamente 100%. Atual: ${totalPercentage.toFixed(2)}%`);
      return;
    }

    setLoading(true);

    try {
      const response = await fetch("/api/proxy/api/split-groups", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name,
          members: members.map(m => ({
            userId: m.userId,
            percentage: Number(m.percentage)
          }))
        })
      });

      if (!response.ok) {
        const errData = await response.text();
        throw new Error(errData || "Não foi possível criar o grupo.");
      }

      onSuccess();
      onClose();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Erro de conexão com o servidor.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-md p-4 animate-fadeIn">
      <div className="bg-[#121214] border border-white/10 w-full max-w-lg rounded-3xl shadow-2xl overflow-hidden text-zinc-100">
        
        {/* Header no estilo Neobank */}
        <div className="px-8 pt-6 pb-4 flex justify-between items-center border-b border-white/5">
          <div>
            <span className="text-xs font-semibold uppercase tracking-wider text-purple-400">Novo Grupo</span>
            <h2 className="text-xl font-bold tracking-tight text-white">Rateio Inteligente</h2>
          </div>
          <button 
            onClick={onClose} 
            className="w-9 h-9 rounded-full bg-white/5 hover:bg-white/10 flex items-center justify-center text-zinc-400 hover:text-white transition-colors"
          >
            ✕
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-8 space-y-6">
          {error && (
            <div className="p-4 bg-red-500/10 border border-red-500/20 rounded-2xl text-red-400 text-sm flex items-center gap-3">
              <span className="w-2 h-2 rounded-full bg-red-400 animate-pulse"></span>
              {error}
            </div>
          )}

          {/* Nome do Grupo */}
          <div className="space-y-2">
            <label className="block text-xs font-medium uppercase tracking-wider text-zinc-400">Identificação do Grupo</label>
            <input 
              type="text" 
              required
              placeholder="Ex: Apartamento 402, Viagem de Férias..." 
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full bg-[#18181b] border border-white/10 rounded-2xl px-5 py-3.5 text-white placeholder-zinc-600 focus:outline-none focus:border-purple-500 focus:ring-1 focus:ring-purple-500 text-sm transition-all"
            />
          </div>

          {/* Membros & Cotas */}
          <div className="space-y-3">
            <div className="flex justify-between items-center">
              <label className="text-xs font-medium uppercase tracking-wider text-zinc-400">Divisão de Cotas</label>
              <div className={`px-3 py-1 rounded-full text-xs font-bold transition-all flex items-center gap-1.5 ${
                isBalanced 
                  ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' 
                  : 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
              }`}>
                <span className={`w-1.5 h-1.5 rounded-full ${isBalanced ? 'bg-emerald-400' : 'bg-amber-400'}`}></span>
                {totalPercentage.toFixed(2)}% / 100%
              </div>
            </div>

            <div className="space-y-2.5 max-h-52 overflow-y-auto pr-1 custom-scrollbar">
              {members.map((member, index) => (
                <div key={index} className="flex gap-3 items-center bg-[#18181b] p-2.5 rounded-2xl border border-white/5">
                  <input 
                    type="text" 
                    required
                    placeholder="UUID do membro (ex: 550e8400...)" 
                    value={member.userId}
                    onChange={(e) => handleMemberChange(index, "userId", e.target.value)}
                    className="flex-1 bg-transparent border-0 px-3 py-1.5 text-white placeholder-zinc-600 focus:outline-none text-xs font-mono"
                  />
                  <div className="flex items-center gap-1 bg-[#121214] px-3 py-1.5 rounded-xl border border-white/10">
                    <input 
                      type="number" 
                      step="0.01"
                      min="0"
                      max="100"
                      required
                      value={member.percentage}
                      onChange={(e) => handleMemberChange(index, "percentage", parseFloat(e.target.value) || 0)}
                      className="w-16 bg-transparent border-0 text-white focus:outline-none text-xs text-right font-bold"
                    />
                    <span className="text-xs text-zinc-500">%</span>
                  </div>
                  {members.length > 1 && (
                    <button 
                      type="button" 
                      onClick={() => handleRemoveMember(index)}
                      className="text-zinc-500 hover:text-red-400 p-1.5 transition-colors"
                    >
                      ✕
                    </button>
                  )}
                </div>
              ))}
            </div>

            <button 
              type="button" 
              onClick={handleAddMember}
              className="text-xs text-purple-400 hover:text-purple-300 font-semibold flex items-center gap-1.5 pt-1 transition-colors"
            >
              + Adicionar participante
            </button>
          </div>

          {/* Footer Actions no estilo Fintech */}
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
              disabled={loading || !isBalanced}
              className={`flex-1 px-5 py-3.5 rounded-2xl text-xs font-bold shadow-lg transition-all ${
                isBalanced && !loading 
                  ? 'bg-[#820AD1] hover:bg-[#6c08ad] text-white shadow-purple-900/30' 
                  : 'bg-zinc-800 text-zinc-600 cursor-not-allowed'
              }`}
            >
              {loading ? "Criando..." : "Concluir Grupo"}
            </button>
          </div>
        </form>

      </div>
    </div>
  );
}