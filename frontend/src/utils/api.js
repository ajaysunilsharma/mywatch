import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  withCredentials: true,
});

export const getWatches = (sort = 'top', page = 0, size = 20) => {
  return api.get(`/watches?sort=${sort}&page=${page}&size=${size}`);
};

export const searchWatches = (query) => {
  return api.get(`/watches/search?query=${query}`);
};

export const getWatch = (id) => {
  return api.get(`/watches/${id}`);
};

export const createWatch = (formData) => {
  return api.post('/watches', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};

export const getReviews = (watchId, page = 0, size = 10) => {
  return api.get(`/watches/${watchId}/reviews?page=${page}&size=${size}`);
};

export const createReview = (watchId, formData) => {
  return api.post(`/watches/${watchId}/reviews`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};

export const voteWatch = (watchId, vote) => {
  return api.post(`/watches/${watchId}/vote`, { vote });
};

export const deleteWatch = (id, token) => {
  return api.delete(`/watches/${id}?token=${token}`);
};

export const deleteReview = (id, token) => {
  return api.delete(`/admin/reviews/${id}?token=${token}`);
};

 export const updateWatch = async (id, formData) => {
   const response = await axios.put(`${API_URL}/watches/${id}`, formData, {
     headers: {
       'Content-Type': 'multipart/form-data',
     },
   });
   return response.data;
 };

export default api;
