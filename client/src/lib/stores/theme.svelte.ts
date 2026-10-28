export type Theme = 'light' | 'dark' | 'playful' | 'xp';

export const THEMES: { value: Theme; label: string }[] = [
	{ value: 'light', label: 'Light' },
	{ value: 'dark', label: 'Dark' },
	{ value: 'playful', label: 'Playful Paws' },
	{ value: 'xp', label: 'Windows XP' }
];

const STORAGE_KEY = 'vethub-theme';
const DEFAULT_THEME: Theme = 'light';

function isTheme(value: string | null): value is Theme {
	return value === 'light' || value === 'dark' || value === 'playful' || value === 'xp';
}

function readStoredTheme(): Theme {
	if (typeof localStorage === 'undefined') return DEFAULT_THEME;
	try {
		const stored = localStorage.getItem(STORAGE_KEY);
		return isTheme(stored) ? stored : DEFAULT_THEME;
	} catch {
		// localStorage can throw in private-browsing/disabled-storage contexts — fall back silently.
		return DEFAULT_THEME;
	}
}

function applyTheme(theme: Theme) {
	if (typeof document === 'undefined') return;
	document.documentElement.setAttribute('data-theme', theme);
}

function createThemeStore() {
	let theme = $state<Theme>(DEFAULT_THEME);

	return {
		get current() {
			return theme;
		},
		init() {
			theme = readStoredTheme();
			applyTheme(theme);
		},
		set(next: Theme) {
			theme = next;
			applyTheme(next);
			try {
				localStorage.setItem(STORAGE_KEY, next);
			} catch {
				// Ignore write failures (e.g. private browsing) — preference just won't persist.
			}
		}
	};
}

export const themeStore = createThemeStore();
