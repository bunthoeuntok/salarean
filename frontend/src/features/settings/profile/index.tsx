import { useLanguage } from '@/context/language-provider'
import { ContentSection } from '../components/content-section'
import { ProfileForm } from './profile-form'

export function SettingsProfile() {
  const { t } = useLanguage()

  return (
    <ContentSection
      title={t.settings.profile.title}
      desc={t.settings.profile.description}
    >
      <ProfileForm />
    </ContentSection>
  )
}
