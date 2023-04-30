import type { Actions, PageServerLoad } from "./$types";
import { BACKEND_URL } from "$env/static/private";
import { fail, redirect, error } from "@sveltejs/kit";
import { z } from "zod";

export const load = (async ({ fetch, depends }) => {
  depends("all-users:load");
  let allUsers;
  await fetch(`http://${BACKEND_URL}/get-all-users`)
    .then((response) => {
      if (!response.ok) {
        throw new Error(response.statusText);
      }
      return response.json();
    })
    .then((data) => {
      allUsers = data.items;
    })
    .catch((error) => {
      console.error("Error fetching /get-all-permissions", error);
    });
  return { users: allUsers };
}) satisfies PageServerLoad;

const schema = z.object({
  username: z
    .string({ required_error: "Username is required" })
    .min(1, { message: "Username is required" })
    .max(64, { message: "Username must be less than 64 characters" })
    .trim(),

  email: z
    .string({ required_error: "Email is required" })
    .email({ message: "Email must be a valid email address" })
    .min(1, { message: "Email is required" })
    .max(64, { message: "Email must be less than 64 characters" })
    .trim(),

  password: z
    .string({ required_error: "Password is required" })
    .min(1, { message: "Password is required" })
    .min(6, { message: "Password must be at least 6 characters" })
    .max(32, { message: "Password must be less than 32 characters" })
    .trim(),

  passwordConfirm: z
    .string({ required_error: "Password is required" })
    .min(1, { message: "Password is required" })
    .min(6, { message: "Password must be at least 6 characters" })
    .max(32, { message: "Password must be less than 32 characters" })
    .trim(),
});

export const actions = {
  createUser: async ({ cookies, request, fetch, locals }) => {
    const formData = await request.formData();
    const zod = schema.safeParse(Object.fromEntries(formData));

    if (formData.get("password") !== formData.get("passwordConfirm")) {
      return fail(400, { error: true, errors: "Passwords do not match" });
    }

    if (!zod.success) {
      // Loop through the errors array and create a custom errors array
      const errors = zod.error.errors.map((error) => {
        return {
          field: error.path[0],
          message: error.message,
        };
      });

      return fail(400, { error: true, errors });
    }

    var requestOptions = {
      method: "POST",
      body: formData,
    };

    await fetch(`http://${BACKEND_URL}/create-user`, requestOptions)
      .then((response) => {
        if (!response.ok) {
          throw new Error(response.statusText);
        }
        return response.json();
      })
      .then((data) => {
        let time = new Date().toLocaleString();
        console.log(
          `${time} : Admin with id: ${locals.user.personId} and username: ${locals.user.username} created a new user`
        );
      })
      .catch((error) => {
        console.error("Error fetching /update-user", error);
      });
  },

  deleteUser: async ({ fetch, request, locals }) => {
    const formData = await request.formData();
    let personId = formData.get("personId");

    let params = new URLSearchParams();
    params.set("personId", personId);

    let parametersString = "?" + params.toString();

    await fetch(`http://${BACKEND_URL}/delete-user` + parametersString, {
      method: "DELETE",
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(response.statusText);
        }
        return response.json();
      })
      .then((data) => {
        let time = new Date().toLocaleString();
        console.log(
          `${time} : Admin with id: ${locals.user.personId} and username: ${locals.user.username} deleted user with id: ${personId}`
        );
      })
      .catch((error) => {
        console.error("Error fetching /update-user", error);
      });
  },
} satisfies Actions;
