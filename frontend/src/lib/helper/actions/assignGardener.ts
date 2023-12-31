import { BACKEND_URL } from "$env/static/private";
import { logger } from "../logger";
import { toasts } from "$stores/toastStore";
import { errorHandler } from "../errorHandler";
import { error, fail } from "@sveltejs/kit";
import { z } from "zod";

/**
 * Assigns a gardener to a sensor station
 * @param event
 * @param formData
 */
export async function assignGardener(event: any, formData?: any) {
  const { request, fetch } = event;
  if (!formData) {
    formData = await request.formData();
  }

  let sensorStationId = String(formData.get("sensorStationId"));
  let gardenerId = String(formData.get("gardener"));

  let params = new URLSearchParams();
  params.set("sensorStationId", sensorStationId);
  params.set("gardenerId", gardenerId);

  if (gardenerId === "") {
    params.set("delete", true.toString());
  }

  await fetch(
    `${BACKEND_URL}/assign-gardener-to-sensor-station?${params.toString()}`,
    { method: "POST" }
  )
    .then(async (res: any) => {
      if (!res.ok) {
        res = await res.json();
        errorHandler(
          event.locals.user?.personId,
          "Error while assigning gardener to sensor station",
          res
        );
      }
      let data = await res.json();
      toasts.addToast(
        event.locals.user?.personId,
        "success",
        "Gardener assigned to sensor station"
      );
    })
    .catch((e: any) => {
      errorHandler(
        event.locals.user?.personId,
        "Error while assigning gardener to sensor station",
        e
      );
      throw error(500, {
        message: "Error while assigning gardener to sensor station",
      });
    });
}
