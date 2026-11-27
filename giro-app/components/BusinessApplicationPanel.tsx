"use client";

import { useEffect, useState } from "react";
import type { FormEvent } from "react";

type Application = {
  cnpj: string;
  status: "PENDING";
  verification?: {
    companyRegistration: "NOT_STARTED" | "WAITING_FOR_PROVIDER";
    representativeIdentity: "NOT_STARTED" | "WAITING_FOR_PROVIDER";
    representativeAuthority: "NOT_STARTED" | "WAITING_FOR_PROVIDER";
  };
};

export default function BusinessApplicationPanel() {
  const [application, setApplication] = useState<Application | null>(null);
  const [cnpj, setCnpj] = useState("");
  const [loading, setLoading] = useState(true);
  const [loadFailed, setLoadFailed] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();
    fetch("/api/proxy/api/business-applications/current", { cache: "no-store", signal: controller.signal })
      .then(async (response) => {
        if (response.status === 404) return null;
        if (!response.ok) throw new Error("Não foi possível consultar a solicitação comercial.");
        return response.json() as Promise<Application>;
      })
      .then(setApplication)
      .catch((caught: unknown) => {
        if (!controller.signal.aborted) {
          setLoadFailed(true);
          setError(caught instanceof Error ? caught.message : "Serviço indisponível.");
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });
    return () => controller.abort();
  }, []);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const response = await fetch("/api/proxy/api/business-applications", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ cnpj }),
      });
      if (!response.ok) {
        if (response.status === 400) throw new Error("Informe um CNPJ com formato e dígitos válidos.");
        if (response.status === 409) throw new Error("Já existe uma solicitação. Atualize a página para consultá-la.");
        throw new Error("Não foi possível enviar a solicitação comercial.");
      }
      setApplication(await response.json() as Application);
      setCnpj("");
    } catch (caught: unknown) {
      setError(caught instanceof Error ? caught.message : "Serviço indisponível.");
    } finally {
      setSaving(false);
    }
  }

  async function requestVerification() {
    setSaving(true);
    setError(null);
    try {
      const response = await fetch("/api/proxy/api/business-applications/current/verification", { method: "POST" });
      if (!response.ok) throw new Error("Não foi possível registrar o pedido de verificação.");
      setApplication(await response.json() as Application);
    } catch (caught: unknown) {
      setError(caught instanceof Error ? caught.message : "Serviço indisponível.");
    } finally {
      setSaving(false);
    }
  }

  const checkLabel = (status: "NOT_STARTED" | "WAITING_FOR_PROVIDER") =>
    status === "NOT_STARTED" ? "não iniciada" : "aguardando provedor";

  return (
    <section className="bg-[#121214] border border-white/10 rounded-3xl p-6 space-y-3">
      <h2 className="text-sm font-bold">Solicitação comercial</h2>
      <p className="text-xs text-zinc-400">A solicitação não cria conta nem libera acesso comercial. A verificação do CNPJ, da identidade e dos poderes do representante ainda depende de provedores.</p>
      {error && <p role="alert" className="text-xs text-red-400">{error}</p>}
      {loading ? <p className="text-xs text-zinc-500">Consultando solicitação...</p> : application ? (
        <div role="status" className="text-xs text-amber-300 space-y-1">
          <p>CNPJ {application.cnpj}: solicitação pendente, ainda não verificada.</p>
          {application.verification && <>
            <p>Cadastro da empresa: {checkLabel(application.verification.companyRegistration)}. Identidade do representante: {checkLabel(application.verification.representativeIdentity)}. Poderes do representante: {checkLabel(application.verification.representativeAuthority)}.</p>
            {application.verification.companyRegistration === "NOT_STARTED" && <button type="button" disabled={saving} onClick={() => void requestVerification()} className="mt-2 text-[#00FF66] underline disabled:opacity-50">Solicitar futura verificação</button>}
            {application.verification.companyRegistration === "WAITING_FOR_PROVIDER" && <p>Pedido registrado; nenhuma verificação foi iniciada e não há liberação de acesso.</p>}
          </>}
        </div>
      ) : !loadFailed && (
        <form onSubmit={submit} className="space-y-3">
          <label htmlFor="business-cnpj" className="block text-xs text-zinc-300">CNPJ da empresa</label>
          <input id="business-cnpj" value={cnpj} onChange={(event) => setCnpj(event.target.value)} maxLength={18} required
            className="w-full rounded-xl border border-white/10 bg-black/30 px-3 py-2 text-sm" />
          <p className="text-xs text-zinc-500">Por enquanto, é permitida uma solicitação por usuário e não é possível alterá-la.</p>
          <button type="submit" disabled={saving} className="rounded-xl border border-[#00FF66]/30 px-4 py-2 text-xs font-bold text-[#00FF66] disabled:opacity-50">
            {saving ? "Enviando..." : "Registrar solicitação"}
          </button>
        </form>
      )}
    </section>
  );
}
