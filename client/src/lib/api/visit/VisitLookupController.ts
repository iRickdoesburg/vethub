/**
 * Visit Lookup API Controller
 *
 * Thin API layer for the public, unauthenticated visit-history lookup.
 * Handles HTTP communication only - no business logic.
 */
import { client } from '$lib/api/client';
import type { OwnerVisitLookupResponse } from '$lib/api/models';

export interface VisitLookupParams {
	lastName: string;
	telephone: string;
}

/** Thrown when the public visit-lookup endpoint rate-limits the caller. */
export class VisitLookupRateLimitedError extends Error {
	constructor() {
		super('Too many lookup attempts. Please wait a moment and try again.');
		this.name = 'VisitLookupRateLimitedError';
	}
}

/**
 * Looks up an owner's pets and visit history by last name + telephone.
 *
 * Returns `null` when no owner matches (a 404 from the backend) - callers should treat this as a
 * normal "no match found" outcome, not an error. Throws {@link VisitLookupRateLimitedError} when
 * rate-limited (429). Any other failure (network error, 5xx, etc.) is re-thrown as-is.
 */
export async function getVisitLookup({
	lastName,
	telephone
}: VisitLookupParams): Promise<OwnerVisitLookupResponse | null> {
	const { data, error, response } = await client.GET('/v1/public/visit-lookup', {
		params: {
			query: { lastName, telephone }
		}
	});

	if (response.status === 404) {
		return null;
	}

	if (response.status === 429) {
		throw new VisitLookupRateLimitedError();
	}

	if (error) throw error;
	if (!data) throw new Error('Failed to look up visit history');
	return data;
}
