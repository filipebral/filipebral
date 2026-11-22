"use client";

import { useState } from "react";
import { simulateInvestment, simulateLoan } from "@/lib/financial-simulations";

const currency = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

function parse(value: string) {
  const normalized = value.trim().replace(",", ".");
  return /^\d+(?:\.\d{1,2})?$/.test(normalized) ? Number(normalized) : NaN;
}

export default function FinancialSimulators() {
  const [kind, setKind] = useState<"loan" | "investment">("loan");
  const [principal, setPrincipal] = useState("");
  const [rate, setRate] = useState("");
  const [months, setMonths] = useState("");
  const [contribution, setContribution] = useState("0");
  const [result, setResult] = useState<string[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  function calculate() {
    setError(null);
    setResult(null);
    try {
      const input = { principal: parse(principal), monthlyRate: parse(rate), months: parse(months) };
      if (kind === "loan") {
        const loan = simulateLoan(input);
        setResult([`Parcela estimada: ${currency.format(loan.installment)}`,
          `Total estimado: ${currency.format(loan.total)}`, `Juros estimados: ${currency.format(loan.interest)}`]);
      } else {
        const investment = simulateInvestment({ ...input, monthlyContribution: parse(contribution) });
        setResult([`Valor final estimado: ${currency.format(investment.total)}`,
          `Rendimento estimado: ${currency.format(investment.interest)}`]);
      }
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Não foi possível calcular.");
    }
  }

  function update(setter: (value: string) => void, value: string) {
    setter(value);
    setResult(null);
    setError(null);
  }

  const fieldClass = "w-full bg-[#18181b] border border-white/10 rounded-2xl px-4 py-3 text-sm";
  return (
    <section className="bg-[#121214] border border-white/10 rounded-3xl p-6 space-y-3">
      <h2 className="text-sm font-bold">Simuladores financeiros</h2>
      <p className="text-xs text-amber-300">Apenas estimativas, sem contratação ou rendimento real. Não incluem impostos, tarifas ou inflação.</p>
      <label htmlFor="simulation-kind" className="block text-xs text-zinc-400">Simulação</label>
      <select id="simulation-kind" value={kind} onChange={(event) => { setKind(event.target.value === "investment" ? "investment" : "loan"); setResult(null); setError(null); }} className={fieldClass}>
        <option value="loan">Empréstimo</option>
        <option value="investment">Investimento</option>
      </select>
      <label htmlFor="simulation-principal" className="block text-xs text-zinc-400">{kind === "loan" ? "Valor do empréstimo" : "Valor inicial"} em reais</label>
      <input id="simulation-principal" inputMode="decimal" value={principal} onChange={(event) => update(setPrincipal, event.target.value)} className={fieldClass} />
      {kind === "investment" && <><label htmlFor="simulation-contribution" className="block text-xs text-zinc-400">Aporte mensal em reais</label><input id="simulation-contribution" inputMode="decimal" value={contribution} onChange={(event) => update(setContribution, event.target.value)} className={fieldClass} /></>}
      <label htmlFor="simulation-rate" className="block text-xs text-zinc-400">Juros mensais (%)</label>
      <input id="simulation-rate" inputMode="decimal" value={rate} onChange={(event) => update(setRate, event.target.value)} className={fieldClass} />
      <label htmlFor="simulation-months" className="block text-xs text-zinc-400">Prazo em meses</label>
      <input id="simulation-months" inputMode="numeric" value={months} onChange={(event) => update(setMonths, event.target.value)} className={fieldClass} />
      <p className="text-xs text-zinc-500">{kind === "loan" ? "Parcelas mensais fixas (Tabela Price)." : "Juros compostos mensais, com aportes no final de cada mês."}</p>
      <button type="button" onClick={calculate} className="w-full border border-[#00FF66]/30 text-[#00FF66] py-3 rounded-2xl text-xs font-bold">Calcular estimativa</button>
      {error && <p role="alert" className="text-xs text-red-400">{error}</p>}
      {result && <div role="status" className="space-y-1 text-xs text-zinc-200">{result.map((line) => <p key={line}>{line}</p>)}</div>}
    </section>
  );
}
