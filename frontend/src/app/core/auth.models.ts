//mirrors java DTOs so front end and back end have the same data structure

export interface UserResponse {
    id: number;
    email: string;
    firstName: string;
    lastName: string;

}

export interface RegisterRequest {
    email: string;
    password: string;
    firstName: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface AuthResponse {
    token: string;
    tokenType: string;
    expiresInSeconds: number;
    user: UserResponse;
}