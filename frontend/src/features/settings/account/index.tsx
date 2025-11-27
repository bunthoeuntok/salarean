import { useLanguage } from '@/context/language-provider'
import { ContentSection } from '../components/content-section'
import { PasswordForm } from './password-form'

export function SettingsAccount() {
  const { t } = useLanguage()

  return (
    <ContentSection
      title={t.settings.account.title}
      desc={t.settings.account.description}
    >
      <PasswordForm />
    </ContentSection>
  )
}
