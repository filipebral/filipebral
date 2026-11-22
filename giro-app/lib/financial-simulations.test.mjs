import { strict as assert } from "node:assert";
import { test } from "node:test";
import { simulateInvestment, simulateLoan } from "./financial-simulations.ts";

test("loan supports zero and positive monthly interest", () => {
  assert.deepEqual(simulateLoan({ principal: 1200, monthlyRate: 0, months: 12 }), {
    installment: 100, total: 1200, interest: 0,
  });
  const result = simulateLoan({ principal: 1000, monthlyRate: 1, months: 12 });
  assert.ok(Math.abs(result.installment - 88.848788678) < 0.000001);
});

test("investment compounds monthly contributions at the end of each month", () => {
  assert.deepEqual(simulateInvestment({ principal: 1000, monthlyRate: 0, months: 2, monthlyContribution: 100 }), {
    total: 1200, interest: 0,
  });
  const result = simulateInvestment({ principal: 1000, monthlyRate: 10, months: 2, monthlyContribution: 100 });
  assert.ok(Math.abs(result.total - 1420) < 0.000001);
});

test("simulators reject invalid inputs and excessively large results", () => {
  assert.throws(() => simulateLoan({ principal: 0, monthlyRate: 1, months: 12 }));
  assert.throws(() => simulateLoan({ principal: 1000, monthlyRate: -1, months: 12 }));
  assert.throws(() => simulateInvestment({ principal: 1000, monthlyRate: 1, months: 1.5, monthlyContribution: 0 }));
  assert.throws(() => simulateInvestment({ principal: 1000, monthlyRate: 100, months: 600, monthlyContribution: 0 }));
});
