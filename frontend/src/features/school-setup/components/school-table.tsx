import { useQuery } from "@tanstack/react-query";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { fetchSchools } from '@/services/location.service'
import { useSchoolSetupStore } from "@/store/school-setup-store";
import { Loader2, Check } from "lucide-react";
import { useLanguage } from "@/context/language-provider";
import { AddSchoolDialog } from "./add-school-dialog";

export function SchoolTable() {
  const { t } = useLanguage();
  const { selectedProvinceId, selectedDistrictId, selectedSchoolId, setSchoolId } =
    useSchoolSetupStore();

  const {
    data: schools,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["schools", selectedDistrictId],
    queryFn: () => fetchSchools(selectedDistrictId!),
    enabled: !!selectedDistrictId,
  });

  if (!selectedDistrictId) {
    return (
      <div className="space-y-2">
        <h3 className="text-sm font-medium text-muted-foreground">Schools</h3>
        <div className="rounded-md border p-8 text-center text-sm text-muted-foreground">
          Select a district to view schools
        </div>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="space-y-2">
        <h3 className="text-sm font-medium">Schools</h3>
        <div className="flex items-center justify-center gap-2 rounded-md border p-8">
          <Loader2 className="h-4 w-4 animate-spin" />
          <span className="text-sm text-muted-foreground">
            Loading schools...
          </span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-2">
        <h3 className="text-sm font-medium">Schools</h3>
        <div className="rounded-md border p-8 text-center text-sm text-destructive">
          Failed to load schools. Please try again.
        </div>
      </div>
    );
  }

  if (schools && schools.length === 0) {
    return (
      <div className="space-y-2">
        <h3 className="text-sm font-medium">{t.schoolSetup.step1.schools}</h3>
        <div className="rounded-md border p-8 text-center">
          <p className="text-sm text-muted-foreground mb-4">
            {t.schoolSetup.step1.noSchools}
          </p>
          {selectedProvinceId && selectedDistrictId && (
            <AddSchoolDialog
              provinceId={selectedProvinceId}
              districtId={selectedDistrictId}
            />
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-medium">
          {t.schoolSetup.step1.schoolsCount.replace('{count}', String(schools?.length || 0))}
        </h3>
        {selectedProvinceId && selectedDistrictId && (
          <AddSchoolDialog
            provinceId={selectedProvinceId}
            districtId={selectedDistrictId}
          />
        )}
      </div>
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>{t.schoolSetup.table.schoolName}</TableHead>
              <TableHead>{t.schoolSetup.table.type}</TableHead>
              <TableHead>{t.schoolSetup.table.address}</TableHead>
              <TableHead className="w-[100px]">{t.schoolSetup.table.action}</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {schools?.map((school) => (
              <TableRow
                key={school.id}
                className={
                  selectedSchoolId === school.id ? "bg-muted/50" : undefined
                }
              >
                <TableCell className="font-medium">
                  {school.name}
                  {school.nameKhmer && (
                    <span className="block text-xs text-muted-foreground">
                      {school.nameKhmer}
                    </span>
                  )}
                </TableCell>
                <TableCell>
                  <span className="inline-flex items-center rounded-full px-2 py-1 text-xs font-medium bg-blue-50 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400">
                    {t.schoolSetup.types[school.type as keyof typeof t.schoolSetup.types] || school.type.replace("_", " ")}
                  </span>
                </TableCell>
                <TableCell className="text-sm text-muted-foreground">
                  {school.address}
                </TableCell>
                <TableCell>
                  {selectedSchoolId === school.id ? (
                    <Button variant="outline" size="sm" disabled>
                      <Check className="mr-2 h-4 w-4" />
                      {t.schoolSetup.table.selectedButton}
                    </Button>
                  ) : (
                    <Button
                      variant="default"
                      size="sm"
                      onClick={() => setSchoolId(school.id)}
                    >
                      {t.schoolSetup.table.selectButton}
                    </Button>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
