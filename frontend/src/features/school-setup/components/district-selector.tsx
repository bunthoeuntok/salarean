import { useQuery } from "@tanstack/react-query";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { fetchDistricts } from '@/services/location.service'
import { useSchoolSetupStore } from "@/store/school-setup-store";
import { Loader2 } from "lucide-react";

export function DistrictSelector() {
  const { selectedProvinceId, selectedDistrictId, setDistrictId } =
    useSchoolSetupStore();

  const {
    data: districts,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["districts", selectedProvinceId],
    queryFn: () => fetchDistricts(selectedProvinceId!),
    enabled: !!selectedProvinceId,
  });

  if (!selectedProvinceId) {
    return (
      <div className="space-y-2">
        <label htmlFor="district" className="text-sm font-medium text-muted-foreground">
          District
        </label>
        <Select disabled>
          <SelectTrigger id="district">
            <SelectValue placeholder="Select a province first" />
          </SelectTrigger>
        </Select>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="space-y-2">
        <label htmlFor="district" className="text-sm font-medium">
          District
        </label>
        <div className="flex items-center gap-2">
          <Loader2 className="h-4 w-4 animate-spin" />
          <span className="text-sm text-muted-foreground">
            Loading districts...
          </span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-2">
        <label htmlFor="district" className="text-sm font-medium">
          District
        </label>
        <div className="text-sm text-destructive">
          Failed to load districts. Please try again.
        </div>
      </div>
    );
  }

  if (districts && districts.length === 0) {
    return (
      <div className="space-y-2">
        <label htmlFor="district" className="text-sm font-medium">
          District
        </label>
        <div className="text-sm text-muted-foreground">
          No districts found for this province.
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-2">
      <label htmlFor="district" className="text-sm font-medium">
        District
      </label>
      <Select value={selectedDistrictId || ""} onValueChange={setDistrictId}>
        <SelectTrigger id="district">
          <SelectValue placeholder="Select a district" />
        </SelectTrigger>
        <SelectContent>
          {districts?.map((district) => (
            <SelectItem key={district.id} value={district.id}>
              {district.name}
              {district.nameKhmer && ` (${district.nameKhmer})`}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}
