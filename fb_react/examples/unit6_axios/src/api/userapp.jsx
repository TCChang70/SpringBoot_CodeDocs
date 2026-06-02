// src/api/users.js — 統一管理 API 呼叫函式
import apiClient from './client';

export const getUsers    = (limit) => apiClient.get('/users', { params: { limit } });
export const getUserById = (id)    => apiClient.get(`/users/${id}`);
export const createUser  = (data)  => apiClient.post('/users', data);
export const updateUser  = (id, data) => apiClient.put(`/users/${id}`, data);
export const deleteUser  = (id)    => apiClient.delete(`/users/${id}`);
