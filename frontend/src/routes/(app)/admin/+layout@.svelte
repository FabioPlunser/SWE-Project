<script lang="ts">
  import { page } from "$app/stores";
  import { fly } from "svelte/transition";

  import PageTransition from "$components/pageTransition.svelte";
  import Mobile from "$helper/Mobile.svelte";
  import Query from "$helper/Query.svelte";

  import AdminNav from "$components/ui/adminNav.svelte";
  import Nav from "$components/ui/Nav.svelte";

  let links = [
    {
      name: "Home",
      path: "/admin",
    },
    {
      name: "Plants",
      path: "/admin/plants",
    },
    {
      name: "Gardener",
      path: "/admin/gardener",
    },
    {
      name: "Users",
      path: "/admin/users",
    },
    {
      name: "Settings",
      path: "/admin/settings",
    },
  ];
</script>

<Nav user={"ADMIN"} />

<PageTransition url={$page.url.pathname}>
  <main class="mx-4">
    <slot />
    <div class="pb-24" />
  </main>
</PageTransition>

<br class="mt-24" />
<div class="fixed bottom-0 mb-4 z-50 w-full">
  <AdminNav />
</div>
