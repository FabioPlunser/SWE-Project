// See https://kit.svelte.dev/docs/types#app
// for information about these interfaces
// and what to do when importing types
declare namespace App {
  // interface Locals {}
  // interface PageData {}
  // interface Platform {}
  // interface PrivateEnv {}
  // interface PublicEnv {}
}

declare module "*.svg?component" {
  import type { ComponentType, SvelteComponentTyped } from "svelte";
  import type { SVGAttributes } from "svelte/elements";

  const content: ComponentType<
    SvelteComponentTyped<SVGAttributes<SVGSVGElement>>
  >;

  export default content;
}

declare module "*.svg?src" {
  const content: string;
  export default content;
}

declare module "*.svg?url" {
  const content: string;
  export default content;
}

declare module "*.svg?url" {
  const content: string;
  export default content;
}

declare interface User {
  personId: string;
  username: string;
  token: string;
  permissions: string[];
  email: string;
}

/**
 * An object representing the visibility of table columns.
 * Each key should be equivalent to the ID of the column.
 * If a column is not present in this object, it is assumed to be visible (true) by default.
 * If a column is present and set to false, it should not be visible.
 *
 * @typedef {Object.<string, boolean>} ColumnVisibility
 */
declare interface ColumnVisibility {
  [column: string]: boolean;
}
