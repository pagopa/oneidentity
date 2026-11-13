import { z } from 'zod';
import { idpUserSchema, SamlAttribute } from '../types/api';

type AgeCalculationResult =
  | { success: true; age: number }
  | { success: false; error: string };

export const calculateAgeFromDateOfBirth = (
  dateOfBirth: string,
  today = new Date()
): AgeCalculationResult => {
  const value = dateOfBirth.trim();

  if (!value) {
    return {
      success: false,
      error: 'Enter dateOfBirth to calculate Age.',
    };
  }

  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return {
      success: false,
      error: 'Enter a valid dateOfBirth in YYYY-MM-DD format.',
    };
  }

  const [year, month, day] = value.split('-').map(Number);
  const birthDate = new Date(`${value}T00:00:00`);

  if (
    year < 1 ||
    birthDate.getFullYear() !== year ||
    birthDate.getMonth() !== month - 1 ||
    birthDate.getDate() !== day
  ) {
    return {
      success: false,
      error: 'Enter a valid dateOfBirth in YYYY-MM-DD format.',
    };
  }

  if (birthDate.getTime() > today.getTime()) {
    return {
      success: false,
      error: 'dateOfBirth cannot be in the future.',
    };
  }

  const isBeforeBirthday =
    today.getMonth() < month - 1 ||
    (today.getMonth() === month - 1 && today.getDate() < day);

  return {
    success: true,
    age: today.getFullYear() - year - (isBeforeBirthday ? 1 : 0),
  };
};

export const userFormSchema = idpUserSchema.required({ age: true }).extend({
  samlAttributes: idpUserSchema.shape.samlAttributes.superRefine(
    (attributes, context) => {
      const dateOfBirth = attributes[SamlAttribute.DATE_OF_BIRTH];
      if (dateOfBirth === undefined) {
        return;
      }

      const result = calculateAgeFromDateOfBirth(dateOfBirth);
      if (!result.success) {
        context.addIssue({
          code: z.ZodIssueCode.custom,
          path: [SamlAttribute.DATE_OF_BIRTH],
          message: result.error,
        });
      }
    }
  ),
});
