<script lang="ts">
  import { scale } from "svelte/transition";
  import { createEventDispatcher, onDestroy } from "svelte";
  const dispatch = createEventDispatcher();
  const close = () => dispatch("close");

  import Modal from "$components/ui/Modal.svelte";
  export let imageRef: string;
  export let open = false;

  function handleKeyDown(event: any) {
    if (event.key === "Escape") {
      close();
    }
  }
</script>

<svelte:window on:keydown={handleKeyDown} />
<!-- svelte-ignore a11y-click-events-have-key-events -->
<div class="modal cursor-pointer" class:modal-open={open} on:click={close}>
  <div transition:scale={{ duration: 150 }} class="w-8/12 rounded-2xl">
    <img
      src={imageRef}
      alt="SensorStationImageBit"
      class="object-contain rounded-2xl"
    />
  </div>
</div>
