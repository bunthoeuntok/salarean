import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { toast } from "sonner";
import { ProvinceSelector } from "./components/province-selector";
import { DistrictSelector } from "./components/district-selector";
import { SchoolTable } from "./components/school-table";
import { useSchoolSetupStore } from "@/store/school-setup-store";
import { createTeacherSchool } from "@/services/school";
import {
  teacherSchoolSchema,
  type TeacherSchoolFormData,
} from "@/lib/validations/school-setup";
import { Loader2, Building2 } from "lucide-react";

export function SchoolSetupPage() {
  const navigate = useNavigate();
  const { selectedSchoolId, reset } = useSchoolSetupStore();
  const [showPrincipalForm, setShowPrincipalForm] = useState(false);

  const form = useForm<TeacherSchoolFormData>({
    resolver: zodResolver(teacherSchoolSchema),
    defaultValues: {
      schoolId: "",
      principalName: "",
      principalGender: undefined,
    },
  });

  const createMutation = useMutation({
    mutationFn: createTeacherSchool,
    onSuccess: () => {
      toast.success("School setup completed successfully!");
      reset();
      navigate({ to: "/" });
    },
    onError: (error: any) => {
      const errorCode = error.response?.data?.errorCode || "INTERNAL_ERROR";
      toast.error(`Failed to complete school setup: ${errorCode}`);
    },
  });

  const handleContinue = () => {
    if (!selectedSchoolId) {
      toast.error("Please select a school to continue");
      return;
    }

    form.setValue("schoolId", selectedSchoolId);
    setShowPrincipalForm(true);
  };

  const onSubmit = (data: TeacherSchoolFormData) => {
    createMutation.mutate(data);
  };

  return (
    <div className="container max-w-5xl py-8">
      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <Building2 className="h-6 w-6" />
            <CardTitle>School Setup</CardTitle>
          </div>
          <CardDescription>
            Complete your profile by selecting your school and providing
            principal information
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {!showPrincipalForm ? (
            <>
              {/* Step 1: Select School */}
              <div className="space-y-4">
                <h3 className="text-lg font-semibold">
                  Step 1: Select Your School
                </h3>
                <div className="grid gap-4 sm:grid-cols-2">
                  <ProvinceSelector />
                  <DistrictSelector />
                </div>
                <SchoolTable />
              </div>

              <div className="flex justify-end pt-4 border-t">
                <Button
                  onClick={handleContinue}
                  disabled={!selectedSchoolId}
                  size="lg"
                >
                  Continue
                </Button>
              </div>
            </>
          ) : (
            <>
              {/* Step 2: Principal Information */}
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-semibold">
                    Step 2: Principal Information
                  </h3>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => setShowPrincipalForm(false)}
                  >
                    Back to School Selection
                  </Button>
                </div>

                <Form {...form}>
                  <form
                    onSubmit={form.handleSubmit(onSubmit)}
                    className="space-y-4"
                  >
                    <FormField
                      control={form.control}
                      name="principalName"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Principal Name</FormLabel>
                          <FormControl>
                            <Input
                              placeholder="Enter principal's full name"
                              {...field}
                            />
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />

                    <FormField
                      control={form.control}
                      name="principalGender"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Principal Gender</FormLabel>
                          <Select
                            onValueChange={field.onChange}
                            defaultValue={field.value}
                          >
                            <FormControl>
                              <SelectTrigger>
                                <SelectValue placeholder="Select gender" />
                              </SelectTrigger>
                            </FormControl>
                            <SelectContent>
                              <SelectItem value="M">Male</SelectItem>
                              <SelectItem value="F">Female</SelectItem>
                            </SelectContent>
                          </Select>
                          <FormMessage />
                        </FormItem>
                      )}
                    />

                    <div className="flex justify-end gap-3 pt-4 border-t">
                      <Button
                        type="button"
                        variant="outline"
                        onClick={() => setShowPrincipalForm(false)}
                      >
                        Back
                      </Button>
                      <Button
                        type="submit"
                        disabled={createMutation.isPending}
                        size="lg"
                      >
                        {createMutation.isPending && (
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        )}
                        Complete Setup
                      </Button>
                    </div>
                  </form>
                </Form>
              </div>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
