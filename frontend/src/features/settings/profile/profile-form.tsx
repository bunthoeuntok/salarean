import { useState, useRef } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { Camera, Loader2, Trash2 } from 'lucide-react'

import { Button } from '@/components/ui/button'
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'

import { useLanguage } from '@/context/language-provider'
import { useAuthStore } from '@/store/auth-store'
import { profileService } from '@/services/profile.service'
import { useValidationSchemas, type ProfileFormData } from '@/hooks/use-validation-schemas'

export function ProfileForm() {
  const { t, translateError, setLanguage } = useLanguage()
  const { user, setUser } = useAuthStore()
  const { profileSchema } = useValidationSchemas()
  const [isLoading, setIsLoading] = useState(false)
  const [isUploadingPhoto, setIsUploadingPhoto] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const form = useForm<ProfileFormData>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      name: user?.name || '',
      email: user?.email || '',
      phoneNumber: user?.phoneNumber || '',
      preferredLanguage: user?.preferredLanguage || 'en',
    },
  })

  async function onSubmit(data: ProfileFormData) {
    setIsLoading(true)
    try {
      const updatedUser = await profileService.updateProfile({
        name: data.name || undefined,
        email: data.email,
        phoneNumber: data.phoneNumber,
        preferredLanguage: data.preferredLanguage,
      })
      setUser(updatedUser)
      setLanguage(data.preferredLanguage)
      toast.success(t.settings.profile.updateSuccess)
    } catch (error) {
      const errorCode = (error as Error).message
      toast.error(translateError(errorCode as Parameters<typeof translateError>[0]))
    } finally {
      setIsLoading(false)
    }
  }

  async function handlePhotoUpload(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    if (!file) return

    // Validate file type
    if (!file.type.startsWith('image/')) {
      toast.error(t.settings.profile.invalidFileType)
      return
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      toast.error(t.settings.profile.fileTooLarge)
      return
    }

    setIsUploadingPhoto(true)
    try {
      const response = await profileService.uploadPhoto(file)
      if (user) {
        setUser({
          ...user,
          profilePhotoUrl: response.profilePhotoUrl,
          profilePhotoUploadedAt: response.profilePhotoUploadedAt,
        })
      }
      toast.success(t.settings.profile.photoUploadSuccess)
    } catch (error) {
      const errorCode = (error as Error).message
      toast.error(translateError(errorCode as Parameters<typeof translateError>[0]))
    } finally {
      setIsUploadingPhoto(false)
      // Reset file input
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
    }
  }

  async function handlePhotoDelete() {
    setIsUploadingPhoto(true)
    try {
      await profileService.deletePhoto()
      if (user) {
        setUser({
          ...user,
          profilePhotoUrl: null,
          profilePhotoUploadedAt: null,
        })
      }
      toast.success(t.settings.profile.photoDeleteSuccess)
    } catch (error) {
      const errorCode = (error as Error).message
      toast.error(translateError(errorCode as Parameters<typeof translateError>[0]))
    } finally {
      setIsUploadingPhoto(false)
    }
  }

  const userInitials = user?.name
    ? user.name
        .split(' ')
        .map((n) => n[0])
        .join('')
        .toUpperCase()
        .slice(0, 2)
    : user?.email?.slice(0, 2).toUpperCase() || 'U'

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className='space-y-8'>
        {/* Profile Photo */}
        <div className='flex items-center gap-x-6'>
          <div className='relative'>
            <Avatar className='h-24 w-24'>
              <AvatarImage src={user?.profilePhotoUrl || undefined} alt={user?.name || 'Profile'} />
              <AvatarFallback className='text-lg'>{userInitials}</AvatarFallback>
            </Avatar>
            {isUploadingPhoto && (
              <div className='absolute inset-0 flex items-center justify-center rounded-full bg-black/50'>
                <Loader2 className='h-6 w-6 animate-spin text-white' />
              </div>
            )}
          </div>
          <div className='space-y-2'>
            <div className='flex gap-2'>
              <Button
                type='button'
                variant='outline'
                size='sm'
                disabled={isUploadingPhoto}
                onClick={() => fileInputRef.current?.click()}
              >
                <Camera className='mr-2 h-4 w-4' />
                {t.settings.profile.changePhoto}
              </Button>
              {user?.profilePhotoUrl && (
                <Button
                  type='button'
                  variant='outline'
                  size='sm'
                  disabled={isUploadingPhoto}
                  onClick={handlePhotoDelete}
                >
                  <Trash2 className='mr-2 h-4 w-4' />
                  {t.settings.profile.removePhoto}
                </Button>
              )}
            </div>
            <p className='text-xs text-muted-foreground'>
              {t.settings.profile.photoHint}
            </p>
            <input
              ref={fileInputRef}
              type='file'
              accept='image/*'
              className='hidden'
              onChange={handlePhotoUpload}
            />
          </div>
        </div>

        {/* Name */}
        <FormField
          control={form.control}
          name='name'
          render={({ field }) => (
            <FormItem>
              <FormLabel>{t.settings.profile.name}</FormLabel>
              <FormControl>
                <Input placeholder={t.settings.profile.namePlaceholder} {...field} />
              </FormControl>
              <FormDescription>{t.settings.profile.nameDescription}</FormDescription>
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Email */}
        <FormField
          control={form.control}
          name='email'
          render={({ field }) => (
            <FormItem>
              <FormLabel>{t.settings.profile.email}</FormLabel>
              <FormControl>
                <Input type='email' placeholder={t.settings.profile.emailPlaceholder} {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Phone Number */}
        <FormField
          control={form.control}
          name='phoneNumber'
          render={({ field }) => (
            <FormItem>
              <FormLabel>{t.settings.profile.phone}</FormLabel>
              <FormControl>
                <Input placeholder={t.settings.profile.phonePlaceholder} {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Preferred Language */}
        <FormField
          control={form.control}
          name='preferredLanguage'
          render={({ field }) => (
            <FormItem>
              <FormLabel>{t.settings.profile.language}</FormLabel>
              <Select onValueChange={field.onChange} defaultValue={field.value}>
                <FormControl>
                  <SelectTrigger>
                    <SelectValue placeholder={t.settings.profile.languagePlaceholder} />
                  </SelectTrigger>
                </FormControl>
                <SelectContent>
                  <SelectItem value='en'>{t.settings.profile.languageEnglish}</SelectItem>
                  <SelectItem value='km'>{t.settings.profile.languageKhmer}</SelectItem>
                </SelectContent>
              </Select>
              <FormDescription>{t.settings.profile.languageDescription}</FormDescription>
              <FormMessage />
            </FormItem>
          )}
        />

        <Button type='submit' disabled={isLoading}>
          {isLoading ? (
            <>
              <Loader2 className='mr-2 h-4 w-4 animate-spin' />
              {t.settings.profile.updating}
            </>
          ) : (
            t.settings.profile.updateButton
          )}
        </Button>
      </form>
    </Form>
  )
}
