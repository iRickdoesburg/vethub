<script lang="ts">
	import { Sun, Moon, PawPrint, Palette, Check, Monitor } from 'lucide-svelte';
	import { Button } from '$lib/components/ui/button';
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
	import { themeStore, THEMES, type Theme } from '$lib/stores/theme.svelte';

	const icons: Record<Theme, typeof Sun> = {
		light: Sun,
		dark: Moon,
		playful: PawPrint,
		xp: Monitor
	};
</script>

<DropdownMenu.Root>
	<DropdownMenu.Trigger>
		{#snippet child({ props })}
			<Button {...props} variant="ghost" size="icon" aria-label="Change theme">
				<Palette class="h-5 w-5" />
			</Button>
		{/snippet}
	</DropdownMenu.Trigger>
	<DropdownMenu.Content align="end">
		{#each THEMES as { value, label } (value)}
			{@const Icon = icons[value]}
			<DropdownMenu.Item onclick={() => themeStore.set(value)} class="justify-between gap-6">
				<span class="flex items-center gap-2">
					<Icon class="h-4 w-4" />
					{label}
				</span>
				{#if themeStore.current === value}
					<Check class="h-4 w-4 text-primary" />
				{/if}
			</DropdownMenu.Item>
		{/each}
	</DropdownMenu.Content>
</DropdownMenu.Root>
