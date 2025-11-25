'use client'

import { useMemo } from 'react'
import { cn } from '@/lib/utils'

type PasswordStrengthProps = {
  password: string
}

type StrengthLevel = 'weak' | 'medium' | 'strong'

interface PasswordCheck {
  label: string
  passed: boolean
}

export function PasswordStrength({ password }: PasswordStrengthProps) {
  const checks = useMemo((): PasswordCheck[] => {
    return [
      { label: 'At least 8 characters', passed: password.length >= 8 },
      { label: 'Uppercase letter', passed: /[A-Z]/.test(password) },
      { label: 'Lowercase letter', passed: /[a-z]/.test(password) },
      { label: 'Number', passed: /[0-9]/.test(password) },
      { label: 'Special character', passed: /[@#$%^&+=!*()_-]/.test(password) },
    ]
  }, [password])

  const passedCount = checks.filter((c) => c.passed).length

  const strength = useMemo((): StrengthLevel => {
    if (passedCount <= 2) return 'weak'
    if (passedCount <= 4) return 'medium'
    return 'strong'
  }, [passedCount])

  const strengthColor = {
    weak: 'bg-destructive',
    medium: 'bg-yellow-500',
    strong: 'bg-green-500',
  }

  const strengthLabel = {
    weak: 'Weak',
    medium: 'Medium',
    strong: 'Strong',
  }

  if (!password) return null

  return (
    <div className="space-y-2">
      {/* Strength bar */}
      <div className="flex gap-1">
        {[0, 1, 2].map((i) => (
          <div
            key={i}
            className={cn(
              'h-1 flex-1 rounded-full transition-colors',
              i === 0 && passedCount > 0 && strengthColor[strength],
              i === 1 && passedCount > 2 && strengthColor[strength],
              i === 2 && passedCount === 5 && strengthColor[strength],
              !(
                (i === 0 && passedCount > 0) ||
                (i === 1 && passedCount > 2) ||
                (i === 2 && passedCount === 5)
              ) && 'bg-muted'
            )}
          />
        ))}
      </div>

      {/* Strength label */}
      <div className="flex justify-between text-xs">
        <span className="text-muted-foreground">Password strength:</span>
        <span
          className={cn(
            'font-medium',
            strength === 'weak' && 'text-destructive',
            strength === 'medium' && 'text-yellow-600',
            strength === 'strong' && 'text-green-600'
          )}
        >
          {strengthLabel[strength]}
        </span>
      </div>

      {/* Requirements checklist */}
      <ul className="space-y-1 text-xs">
        {checks.map((check) => (
          <li
            key={check.label}
            className={cn(
              'flex items-center gap-2',
              check.passed ? 'text-green-600' : 'text-muted-foreground'
            )}
          >
            <span>{check.passed ? '✓' : '○'}</span>
            <span>{check.label}</span>
          </li>
        ))}
      </ul>
    </div>
  )
}
