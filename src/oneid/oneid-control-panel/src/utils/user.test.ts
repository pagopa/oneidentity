import { describe, expect, it } from 'vitest';
import { calculateAgeFromDateOfBirth } from './user';

describe('calculateAgeFromDateOfBirth', () => {
  const today = new Date(2026, 10, 13, 12);

  it.each([
    ['2000-11-12', 26],
    ['2000-11-13', 26],
    ['2000-11-14', 25],
    ['2026-11-13', 0],
    ['2026-01-01', 0],
    ['2000-02-29', 26],
    ['1900-02-28', 126],
    ['0001-01-01', 2025],
    [' 2000-01-01', 26],
    ['2000-01-01 ', 26],
  ])('calculates completed years for %s', (dateOfBirth, age) => {
    expect(calculateAgeFromDateOfBirth(dateOfBirth, today)).toEqual({
      success: true,
      age,
    });
  });

  it.each([
    [new Date(2024, 1, 28, 12), 23],
    [new Date(2024, 1, 29, 12), 24],
    [new Date(2024, 2, 1, 12), 24],
    [new Date(2025, 1, 28, 12), 24],
    [new Date(2025, 2, 1, 12), 25],
  ])('handles leap-day birthdays on %s', (referenceDate, age) => {
    expect(calculateAgeFromDateOfBirth('2000-02-29', referenceDate)).toEqual({
      success: true,
      age,
    });
  });

  it.each([
    '',
    ' ',
    '2000-1-01',
    '2000-01-1',
    '01-01-2000',
    '2000/01/01',
    '2000-01-01T00:00:00Z',
    'not-a-date',
    '2000-00-01',
    '2000-13-01',
    '2000-01-00',
    '2000-01-32',
    '2000-04-31',
    '2025-02-29',
    '1900-02-29',
    '0000-01-01',
    '2026-11-14',
    '2027-01-01',
  ])('rejects invalid or future dates (%j)', (dateOfBirth) => {
    const result = calculateAgeFromDateOfBirth(dateOfBirth, today);

    expect(result).toEqual({
      success: false,
      error: expect.any(String),
    });
    if (!result.success) {
      expect(result.error).not.toBe('');
    }
  });

  it('compares calendar dates without rejecting a birth earlier today', () => {
    expect(
      calculateAgeFromDateOfBirth('2026-11-13', new Date(2026, 10, 13, 0, 0, 1))
    ).toEqual({ success: true, age: 0 });
  });
});
