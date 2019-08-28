import React from 'react';
import { usePromise, getProduct } from './api';

export default function ProductDetail({match}) {
  const uuid = match.params.uuid;
  const response = usePromise(() => getProduct(uuid), [uuid]);
  if (!response) {
    return null;
  }
  const product = response.node;
  return (
    <div className="product-detail">
      <h1>{product.fields.name}</h1>
      <div className="row">
        <div className="col-md-6">
          <form>
            <div className="form-group">
              <label htmlFor="name">Name</label>
              <input className="form-control" id="name" name="name" placeholder="Name" defaultValue={product.fields.name} />
            </div>

            <div className="form-group">
              <label htmlFor="description">Description</label>
              <textarea className="form-control" id="description" name="description" placeholder="Description" defaultValue={product.fields.description} rows="7"></textarea>
            </div>

            <div className="form-group">
              <label htmlFor="sku">SKU</label>
              <input className="form-control" id="sku" name="SKU" placeholder="SKU" defaultValue={product.fields.SKU} type="number" />
            </div>

            <div className="row">
              <div className="col-sm-4 form-group">
                <label htmlFor="price">Price</label>
                <input className="form-control" id="price" min="0" name="price" step="0.01" type="number" defaultValue={product.fields.price} />
              </div>
              <div className="col-sm-4 form-group">
                <label htmlFor="weight">Weight</label>
                <input className="form-control" id="weight" min="0" name="weight" type="number" defaultValue={product.fields.weight} />
              </div>
              <div className="col-sm-4 form-group">
                <label htmlFor="stocklevel">Stock Level</label>
                <input className="form-control" id="stocklevel" min="0" name="stocklevel" type="number" defaultValue={product.fields.stocklevel} />
              </div>
            </div>

            <div className="row">
              <div className="col-xs-4">
                <button className="btn btn-primary btn-lg" type="submit" disabled>Submit</button>
              </div>
            </div>

          </form>
        </div>
        <div className="col-md-6">
          <img className="img-thumbnail" src={`/api/v1/demo/webroot/${product.fields.vehicleImage.path}`} alt="" />
        </div>
      </div>
    </div>
  );
}