import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { createWatch } from '../utils/api';
import { loadModel, validateWatchImage } from '../utils/imageValidator';
import './AddWatch.css';

function AddWatch() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    brand: '',
    model: '',
    year: '',
    description: '',
    createdBy: ''
  });
  const [image, setImage] = useState(null);
  const [preview, setPreview] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [modelReady, setModelReady] = useState(false);
  const [isValidating, setIsValidating] = useState(false);

  useEffect(() => {
    loadModel().then(() => setModelReady(true));
  }, []);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleImageChange = async (e) => {
    const file = e.target.files[0];
    if (file) {
      setIsValidating(true);
      setError('Verifying image with AI...');

      const result = await validateWatchImage(file);

      if (result.isValid) {
        setImage(file);
        setPreview(result.previewUrl);
        setError('');
      } else {
        setError(result.error);
        setPreview(null);
        setImage(null);
      }
      setIsValidating(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.brand || !formData.model || !formData.description) {
      setError('Brand, model, and description are required');
      return;
    }

    setSubmitting(true);
    setError('');

    try {
      const data = new FormData();
      data.append('brand', formData.brand);
      data.append('model', formData.model);
      if (formData.year) data.append('year', formData.year);
      data.append('description', formData.description);
      data.append('createdBy', formData.createdBy || 'Anonymous');
      if (image) data.append('image', image);

      const response = await createWatch(data);
      navigate(`/watch/${response.data.id}`);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to create watch. It may already exist.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="add-watch">
      <div className="container">
        <div className="add-watch-header">
          <h1>Add a Watch</h1>
          <p>Share your favorite timepiece with the community</p>
        </div>

        <form onSubmit={handleSubmit} className="add-watch-form">
          {error && <div className="error-message">{error}</div>}

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="brand">Brand *</label>
              <input
                type="text"
                id="brand"
                name="brand"
                value={formData.brand}
                onChange={handleChange}
                placeholder="e.g., Rolex"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="model">Model *</label>
              <input
                type="text"
                id="model"
                name="model"
                value={formData.model}
                onChange={handleChange}
                placeholder="e.g., Submariner"
                required
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="year">Year</label>
              <input
                type="number"
                id="year"
                name="year"
                value={formData.year}
                onChange={handleChange}
                placeholder="e.g., 2020"
                min="1800"
                max={new Date().getFullYear()}
              />
            </div>

            <div className="form-group">
              <label htmlFor="createdBy">Your Name</label>
              <input
                type="text"
                id="createdBy"
                name="createdBy"
                value={formData.createdBy}
                onChange={handleChange}
                placeholder="Anonymous"
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="description">Description *</label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="Tell us what makes this watch special..."
              rows="5"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="image">
              Image (max 5MB)
              {!modelReady && <span style={{ marginLeft: '8px', fontSize: '0.85em', color: '#666' }}>(Initializing AI...)</span>}
            </label>
            <input
              type="file"
              id="image"
              accept="image/jpeg,image/jpg,image/png"
              onChange={handleImageChange}
              disabled={!modelReady || isValidating}
            />
            {preview && (
              <div className="image-preview">
                <img src={preview} alt="Preview" />
              </div>
            )}
          </div>

          <div className="form-actions">
            <button 
              type="button" 
              onClick={() => navigate('/')}
              className="btn-outline"
            >
              Cancel
            </button>
            <button 
              type="submit" 
              disabled={submitting}
              className="btn-primary"
            >
              {submitting ? 'Adding Watch...' : 'Add Watch'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default AddWatch;
