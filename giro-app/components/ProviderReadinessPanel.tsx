"use client";

import { useEffect, useState } from "react";

type Readiness = {
  accountAndPayments: string;
  cards: string;
  credit: string;
  investments: string;
  insurance: string;
  notifications: string;
  notificationChannels: string[];
};

const labels: Record<keyof Omit<Readiness, "notificationChannels">, string> = {
  accountAndPayments: "Contas e pagamentos",
  cards: "Cartões",
  credit: "Crédito",
  investments: "Investimentos",
  insurance: "Seguros",
  notifications: "Notificações externas",
};

export default function ProviderReadinessPanel() {
  const [readiness, setReadiness] = useState<Readiness | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetch("/api/proxy/simulation/providers/readiness", { cache: "no-store" })
      .then(async (response) => {
        if (!response.ok) throw new Error("Não foi possível consultar a configuração de provedores.");
        return response.json() as Promise<Readiness>;
      })
      .then(setReadiness)
      .catch((caught: unknown) => setError(caught instanceof Error ? caught.message : "Serviço de provedores indisponível."));
  }, []);

  return (
    <section className="bg-[#121214] border border-white/10 rounded-3xl p-6 space-y-3">
      <h2 className="text-sm font-bold">Integrações externas</h2>
      <p className="text-xs text-zinc-400">Os contratos estão preparados; nenhum provedor ou serviço de envio está conectado. As funções disponíveis continuam sendo simulações.</p>
      {error && <p role="alert" className="text-xs text-red-400">{error}</p>}
      {!readiness ? !error && <p className="text-xs text-zinc-500">Consultando configuração...</p> : Object.entries(labels).map(([field, label]) => (
        <div key={field} className="flex justify-between gap-3 text-xs">
          <span>{label}</span>
          <span className="text-amber-300">{readiness[field as keyof typeof labels] === "ADAPTER_REGISTERED" ? "Adaptador registrado; aprovação pendente" : "Não configurado"}</span>
        </div>
      ))}
    </section>
  );
}
