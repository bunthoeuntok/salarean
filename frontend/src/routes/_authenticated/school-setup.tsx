import { createFileRoute } from "@tanstack/react-router";
import { SchoolSetupPage } from "@/features/school-setup";

export const Route = createFileRoute("/_authenticated/school-setup")({
  component: SchoolSetupPage,
});
