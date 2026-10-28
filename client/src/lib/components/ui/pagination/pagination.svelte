<script lang="ts">
	import { Button } from '$lib/components/ui/button';
	import { ChevronLeft, ChevronRight } from 'lucide-svelte';

	interface Props {
		/** Zero-based current page index */
		page: number;
		totalPages: number;
		onPageChange: (page: number) => void;
	}

	let { page, totalPages, onPageChange }: Props = $props();
</script>

{#if totalPages > 1}
	<div class="flex items-center justify-end gap-2">
		<Button
			variant="outline"
			size="sm"
			disabled={page <= 0}
			onclick={() => onPageChange(page - 1)}
			class="gap-1"
		>
			<ChevronLeft class="h-4 w-4" />
			Prev
		</Button>
		<span class="text-sm text-muted-foreground">
			Page {page + 1} of {totalPages}
		</span>
		<Button
			variant="outline"
			size="sm"
			disabled={page >= totalPages - 1}
			onclick={() => onPageChange(page + 1)}
			class="gap-1"
		>
			Next
			<ChevronRight class="h-4 w-4" />
		</Button>
	</div>
{/if}
