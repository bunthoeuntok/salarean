import { useQuery } from "@tanstack/react-query";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { fetchProvinces } from "@/services/location";
import { useSchoolSetupStore } from "@/store/school-setup-store";
import { Loader2 } from "lucide-react";

export function ProvinceSelector() {
  const { selectedProvinceId, setProvinceId } = useSchoolSetupStore();

  const { data: provinces, isLoading, error } = useQuery({
    queryKey: ["provinces"],
    queryFn: fetchProvinces,
  });

  if (isLoading) {
    return (
      <div className="flex items-center gap-2">
        <Loader2 className="h-4 w-4 animate-spin" />
        <span className="text-sm text-muted-foreground">
          Loading provinces...
        </span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-sm text-destructive">
        Failed to load provinces. Please try again.
      </div>
    );
  }

  return (
    <div className="space-y-2">
      <label htmlFor="province" className="text-sm font-medium">
        Province
      </label>
      <Select value={selectedProvinceId || ""} onValueChange={setProvinceId}>
        <SelectTrigger id="province">
          <SelectValue placeholder="Select a province" />
        </SelectTrigger>
        <SelectContent>
          {provinces?.map((province) => (
            <SelectItem key={province.id} value={province.id}>
              {province.name}
              {province.nameKhmer && ` (${province.nameKhmer})`}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}
