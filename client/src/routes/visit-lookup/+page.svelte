<script lang="ts">
	import { getVisitLookup, VisitLookupRateLimitedError } from '$lib/api/visit/VisitLookupController';
	import type { PetVisitLookupResponse } from '$lib/api/models';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import { Badge } from '$lib/components/ui/badge';
	import { Search, PawPrint, Calendar, Loader2, AlertCircle } from 'lucide-svelte';
	import { toast } from 'svelte-sonner';

	let lastName = $state('');
	let telephone = $state('');
	let submitting = $state(false);

	// null = no search performed yet, [] = searched but no pets found, PetVisitLookupResponse[] = results
	let pets = $state<PetVisitLookupResponse[] | null>(null);
	let notFound = $state(false);

	function formatDate(dateStr: string | undefined): string {
		if (!dateStr) return 'Unknown date';
		return new Date(dateStr).toLocaleDateString('en-US', {
			year: 'numeric',
			month: 'long',
			day: 'numeric'
		});
	}

	async function handleSubmit(e: Event) {
		e.preventDefault();
		submitting = true;
		notFound = false;
		pets = null;

		try {
			const result = await getVisitLookup({
				lastName: lastName.trim(),
				telephone: telephone.trim()
			});

			if (result === null) {
				notFound = true;
			} else {
				pets = result.pets ?? [];
			}
		} catch (err) {
			if (err instanceof VisitLookupRateLimitedError) {
				toast.error(err.message);
			} else {
				notFound = true;
				console.error('Error looking up visit history:', err);
			}
		} finally {
			submitting = false;
		}
	}
</script>

<svelte:head>
	<title>Look Up Visit History | VetHub</title>
</svelte:head>

<div class="container mx-auto max-w-2xl px-4 py-8">
	<!-- Header -->
	<div class="mb-8 flex items-center gap-3">
		<div class="flex h-12 w-12 items-center justify-center rounded-lg bg-accent/10">
			<PawPrint class="h-6 w-6 text-accent" />
		</div>
		<div>
			<h1 class="text-2xl font-bold text-foreground">Look Up Your Pet's Visit History</h1>
			<p class="text-sm text-muted-foreground">
				Enter the details we have on file to view past visits
			</p>
		</div>
	</div>

	<!-- Lookup form -->
	<div class="card p-6">
		<form onsubmit={handleSubmit} class="space-y-6">
			<div class="space-y-2">
				<Label for="lastName">Last Name</Label>
				<Input
					id="lastName"
					type="text"
					placeholder="e.g. Franklin"
					bind:value={lastName}
					required
					disabled={submitting}
				/>
			</div>

			<div class="space-y-2">
				<Label for="telephone">Telephone</Label>
				<Input
					id="telephone"
					type="tel"
					placeholder="e.g. 6085551023"
					bind:value={telephone}
					required
					disabled={submitting}
				/>
			</div>

			<div class="flex justify-end">
				<Button type="submit" disabled={submitting} class="gap-2">
					{#if submitting}
						<Loader2 class="h-4 w-4 animate-spin" />
					{:else}
						<Search class="h-4 w-4" />
					{/if}
					Find My Pets
				</Button>
			</div>
		</form>
	</div>

	<!-- Results -->
	{#if notFound}
		<div class="card mt-6 flex items-start gap-3 p-6">
			<AlertCircle class="mt-0.5 h-5 w-5 shrink-0 text-warning" />
			<p class="text-sm text-muted-foreground">
				We couldn't find a match. Please check your details, or call the clinic.
			</p>
		</div>
	{:else if pets !== null}
		{#if pets.length === 0}
			<div class="card mt-6 p-8 text-center">
				<PawPrint class="mx-auto mb-4 h-12 w-12 text-muted-foreground/50" />
				<p class="text-muted-foreground">No pets found on this account</p>
			</div>
		{:else}
			<div class="mt-6 space-y-6">
				{#each pets as pet (pet.name)}
					<div class="card p-6">
						<div class="mb-4 flex items-center gap-2">
							<PawPrint class="h-5 w-5 text-accent" />
							<h2 class="text-lg font-semibold text-foreground">{pet.name}</h2>
							<Badge variant="secondary">{pet.type ?? 'Unknown type'}</Badge>
						</div>

						{#if !pet.visits?.length}
							<p class="text-sm text-muted-foreground">No visits recorded for this pet</p>
						{:else}
							<div class="space-y-3">
								{#each pet.visits as visit, index (index)}
									<div class="flex items-start gap-3 border-b border-border pb-3 last:border-0 last:pb-0">
										<Calendar class="mt-0.5 h-4 w-4 shrink-0 text-muted-foreground" />
										<div>
											<p class="font-medium text-foreground">{visit.description}</p>
											<p class="text-sm text-muted-foreground">{formatDate(visit.date)}</p>
										</div>
									</div>
								{/each}
							</div>
						{/if}
					</div>
				{/each}
			</div>
		{/if}
	{/if}
</div>
