type Input = { principal: number; monthlyRate: number; months: number };
type Result = { total: number; interest: number };

function validate({ principal, monthlyRate, months }: Input) {
  if (!Number.isFinite(principal) || principal <= 0 || principal > 999_999_999.99
      || !Number.isFinite(monthlyRate) || monthlyRate < 0 || monthlyRate > 100
      || !Number.isInteger(months) || months < 1 || months > 600) {
    throw new Error("Informe valor positivo, juros mensais entre 0% e 100% e prazo de 1 a 600 meses.");
  }
}

function checked(result: Result): Result {
  if (!Number.isFinite(result.total) || result.total > 999_999_999_999_999) {
    throw new Error("O resultado é muito alto para esta simulação.");
  }
  return result;
}

export function simulateLoan(input: Input): Result & { installment: number } {
  validate(input);
  const rate = input.monthlyRate / 100;
  const installment = rate === 0
    ? input.principal / input.months
    : input.principal * rate / -Math.expm1(-input.months * Math.log1p(rate));
  const total = installment * input.months;
  return { ...checked({ total, interest: total - input.principal }), installment };
}

export function simulateInvestment(input: Input & { monthlyContribution: number }): Result {
  validate(input);
  if (!Number.isFinite(input.monthlyContribution) || input.monthlyContribution < 0
      || input.monthlyContribution > 999_999_999.99) {
    throw new Error("Informe um aporte mensal entre zero e R$ 999.999.999,99.");
  }
  const rate = input.monthlyRate / 100;
  const growthMinusOne = Math.expm1(input.months * Math.log1p(rate));
  const contributions = rate === 0 ? input.months : growthMinusOne / rate;
  const total = input.principal * (1 + growthMinusOne) + input.monthlyContribution * contributions;
  return checked({ total, interest: total - input.principal - input.monthlyContribution * input.months });
}
