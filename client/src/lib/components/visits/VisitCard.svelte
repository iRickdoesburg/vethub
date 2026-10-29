<script lang="ts">
	import * as Card from '$lib/components/ui/card';
	import type { VetSummaryResponse } from '$lib/api/models';
	import { Stethoscope, User } from 'lucide-svelte';

	interface Props {
		date: string;
		description: string;
		vet?: VetSummaryResponse | null;
	}

	let { date, description, vet }: Props = $props();

	function formatDate(dateStr: string | undefined): string {
		if (!dateStr) return 'Unknown';
		return new Date(dateStr).toLocaleDateString('en-US', {
			year: 'numeric',
			month: 'long',
			day: 'numeric'
		});
	}
</script>

<Card.Root>
	<Card.Content class="pt-6">
		<div class="flex items-start justify-between">
			<div class="flex items-start gap-4">
				<div class="flex h-10 w-10 items-center justify-center rounded-full bg-success/10">
					<Stethoscope class="h-5 w-5 text-success" />
				</div>
				<div>
					<p class="font-medium text-foreground">{description}</p>
					<p class="text-sm text-muted-foreground">{formatDate(date)}</p>
					<p class="mt-1 flex items-center gap-1.5 text-sm">
						<User class="h-3.5 w-3.5 text-muted-foreground" />
						{#if vet}
							<span class="text-foreground">Dr. {vet.firstName} {vet.lastName}</span>
						{:else}
							<span class="text-muted-foreground italic">Vet not recorded</span>
						{/if}
					</p>
				</div>
			</div>
		</div>
	</Card.Content>
</Card.Root>
