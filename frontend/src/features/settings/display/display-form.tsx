import { useState } from 'react'
import { useLanguage } from '@/context/language-provider'
import { useDisplayStore, type TextSize } from '@/store/display-store'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Type } from 'lucide-react'
import { toast } from 'sonner'

export function DisplayForm() {
  const { t } = useLanguage()
  const { textSize, setTextSize } = useDisplayStore()
  const [selectedSize, setSelectedSize] = useState<TextSize>(textSize)

  const handleSave = () => {
    setTextSize(selectedSize)
    toast.success(t.settings.display.updateSuccess)
  }

  const hasChanges = selectedSize !== textSize

  const textSizeOptions = [
    {
      value: 'small' as TextSize,
      label: t.settings.display.small,
      iconSize: 14,
    },
    {
      value: 'medium' as TextSize,
      label: t.settings.display.medium,
      iconSize: 16,
    },
    {
      value: 'large' as TextSize,
      label: t.settings.display.large,
      iconSize: 18,
    },
  ]

  return (
    <div className='space-y-6'>
      <div className='space-y-4'>
        <div>
          <Label className='text-base'>{t.settings.display.textSize}</Label>
          <p className='text-sm text-muted-foreground mt-1'>
            {t.settings.display.textSizeDescription}
          </p>
        </div>

        <Select value={selectedSize} onValueChange={(value) => setSelectedSize(value as TextSize)}>
          <SelectTrigger className='w-full max-w-md'>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {textSizeOptions.map((option) => (
              <SelectItem key={option.value} value={option.value}>
                <div className='flex items-center gap-2'>
                  <Type size={option.iconSize} className='flex-shrink-0' />
                  <span>{option.label}</span>
                </div>
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <Button onClick={handleSave} disabled={!hasChanges}>
        {t.common.save}
      </Button>
    </div>
  )
}
