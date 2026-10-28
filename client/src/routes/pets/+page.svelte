<script lang="ts">
	import { getPets } from '$lib/api/pet/PetController';
	import { getOwners } from '$lib/api/owner/OwnerController';
	import type { PetResponse, OwnerResponse } from '$lib/api/models';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import { Badge } from '$lib/components/ui/badge';
	import { Button } from '$lib/components/ui/button';
	import { Pagination } from '$lib/components/ui/pagination';
	import * as Table from '$lib/components/ui/table';
	import { PawPrint, Search, User, Cake } from 'lucide-svelte';
	import { toast } from 'svelte-sonner';

	const PAGE_SIZE = 10;

	let pets = $state<PetResponse[]>([]);
	let owners = $state<OwnerResponse[]>([]);
	let loading = $state(true);
	let searchQuery = $state('');
	let page = $state(0);
	let totalPages = $state(0);
	let totalElements = $state(0);

	// Map of ownerId -> owner display name, used to show the owner without
	// requiring the receptionist to navigate through the owner first.
	let ownerNamesById = $derived(() => {
		const map = new Map<number, string>();
		for (const owner of owners) {
			if (owner.id != null) {
				map.set(owner.id, `${owner.firstName ?? ''} ${owner.lastName ?? ''}`.trim());
			}
		}
		return map;
	});

	function ownerName(ownerId: number | undefined): string {
		if (ownerId == null) return 'Unknown owner';
		return ownerNamesById().get(ownerId) ?? 'Unknown owner';
	}

	function calculateAge(birthDate: string | undefined): string {
		if (!birthDate) return 'Unknown age';
		const birth = new Date(birthDate);
		const now = new Date();
		const years = Math.floor((now.getTime() - birth.getTime()) / (365.25 * 24 * 60 * 60 * 1000));
		if (years === 0) {
			const months = Math.floor((now.getTime() - birth.getTime()) / (30.44 * 24 * 60 * 60 * 1000));
			return months <= 1 ? '< 1 month' : `${months} months`;
		}
		return years === 1 ? '1 year' : `${years} years`;
	}

	async function loadPets() {
		loading = true;
		try {
			const [petsPage, ownersResult] = await Promise.all([
				getPets({ name: searchQuery.trim() || undefined, page, size: PAGE_SIZE }),
				owners.length > 0 ? Promise.resolve(owners) : getOwners()
			]);
			pets = petsPage.content ?? [];
			totalPages = petsPage.totalPages ?? 0;
			totalElements = petsPage.totalElements ?? 0;
			owners = ownersResult;
		} catch (err) {
			toast.error('Failed to load pets');
			console.error('Error loading pets:', err);
		} finally {
			loading = false;
		}
	}

	function handleSearchChange() {
		page = 0;
		loadPets();
	}

	function handlePageChange(newPage: number) {
		page = newPage;
		loadPets();
	}

	// Load pets on mount
	$effect(() => {
		loadPets();
	});
</script>

<svelte:head>
	<title>Pets | VetHub</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<!-- Header -->
	<div class="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
		<div class="flex items-center gap-3">
			<div class="flex h-12 w-12 items-center justify-center rounded-lg bg-accent/10">
				<PawPrint class="h-6 w-6 text-accent" />
			</div>
			<div>
				<h1 class="text-2xl font-bold text-foreground">Pets</h1>
				<p class="text-sm text-muted-foreground">Look up patients directly, without going through an owner</p>
			</div>
		</div>
	</div>

	<!-- Search -->
	<div class="mb-6">
		<div class="max-w-md space-y-2">
			<Label for="pet-search">Search pets</Label>
			<div class="relative">
				<Search class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-foreground" />
				<Input
					id="pet-search"
					type="search"
					placeholder="Search by pet name..."
					bind:value={searchQuery}
					oninput={handleSearchChange}
					class="pl-10 placeholder:text-foreground"
				/>
			</div>
		</div>
	</div>

	<!-- Table -->
	{#if loading}
		<div class="card p-12 text-center">
			<div class="mx-auto mb-4 h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
			<p class="text-muted-foreground">Loading pets...</p>
		</div>
	{:else if pets.length === 0}
		<div class="card p-12 text-center">
			<PawPrint class="mx-auto mb-4 h-12 w-12 text-muted-foreground/50" />
			{#if searchQuery}
				<p class="text-muted-foreground">No pets found matching "{searchQuery}"</p>
			{:else}
				<p class="text-muted-foreground">No pets registered yet</p>
			{/if}
		</div>
	{:else}
		<div class="card overflow-hidden">
			<Table.Root>
				<Table.Header>
					<Table.Row>
						<Table.Head>Name</Table.Head>
						<Table.Head>Type</Table.Head>
						<Table.Head>Age</Table.Head>
						<Table.Head>Owner</Table.Head>
						<Table.Head class="w-[100px]">Actions</Table.Head>
					</Table.Row>
				</Table.Header>
				<Table.Body>
					{#each pets as pet (pet.id)}
						<Table.Row class="hover:bg-muted/50">
							<Table.Cell>
								<a href="/pets/{pet.id}" class="flex items-center gap-2 font-medium text-foreground hover:text-primary">
									<PawPrint class="h-3.5 w-3.5 text-accent" />
									{pet.name}
								</a>
							</Table.Cell>
							<Table.Cell>
								<Badge variant="secondary">{pet.type?.name ?? 'Unknown type'}</Badge>
							</Table.Cell>
							<Table.Cell>
								<div class="flex items-center gap-2 text-sm text-muted-foreground">
									<Cake class="h-3.5 w-3.5" />
									{calculateAge(pet.birthDate)}
								</div>
							</Table.Cell>
							<Table.Cell>
								<a
									href="/owners/{pet.ownerId}"
									class="flex items-center gap-2 text-sm text-muted-foreground hover:text-primary"
								>
									<User class="h-3.5 w-3.5" />
									{ownerName(pet.ownerId)}
								</a>
							</Table.Cell>
							<Table.Cell>
								<Button variant="ghost" size="sm" href="/pets/{pet.id}">
									View
								</Button>
							</Table.Cell>
						</Table.Row>
					{/each}
				</Table.Body>
			</Table.Root>
		</div>
		<div class="mt-4 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
			<p class="text-sm text-muted-foreground">
				Showing {pets.length} of {totalElements} pets
			</p>
			<Pagination {page} {totalPages} onPageChange={handlePageChange} />
		</div>
	{/if}
</div>
