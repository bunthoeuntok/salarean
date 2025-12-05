import { useLanguage } from '@/context/language-provider'
import { ContentSection } from '../components/content-section'
import { DisplayForm } from './display-form'

export function SettingsDisplay() {
  const { t } = useLanguage()

  return (
    <ContentSection
      title={t.settings.display.title}
      desc={t.settings.display.description}
    >
      <DisplayForm />
    </ContentSection>
  )
}
