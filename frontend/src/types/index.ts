export interface Event {
    id: number;
    state: string;
    lux: number;
    temp: number;
    phone: string;
    timestamp: number;
}

export interface EventsState {
    events: Event[];
    page: number;
    perPage: number;
    total: number;
}

export interface RootState {
    auth: AuthState;
    events: EventsState;
}

export interface AuthState {
    token: string | null;
}