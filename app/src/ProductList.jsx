import React from 'react';
import { Link } from 'react-router-dom';
import { usePromise, getProducts } from './api';

export default function ProductList({ match }) {
  const uuid = match.params.uuid;
  const productResponse = usePromise(() => getProducts(uuid), [uuid])
  if (!productResponse) {
    return null;
  }
  const category = productResponse.node;
  return (
    <div className="product-list">
      <h1>{category.fields.name}</h1>
      <p>{category.fields.description}</p>
      <div className="row">
        {category.children.elements.map(product => (
          <Product product={product} key={product.uuid} />
        ))}
      </div>
    </div>
  );
}

function Product({ product }) {
  return (
    <div className="product-row col-xs-12 col-sm-6 col-md-4">
      <div className="panel panel-default">
        <div className="panel-body">
          <h3>
            <Link to={`/product/${product.uuid}`}>{product.fields.name}</Link>
            {" "}
            <small>{product.fields.SKU}</small>
          </h3>

          <Link to={`/product/${product.uuid}`}>
            <img alt="" className="img-thumbnail" src={`/api/v1/demo/webroot${product.fields.vehicleImage.path}?w=328`} />
          </Link>
          <p className="description">{product.fields.description}</p>

          <hr />

          <div className="row">
            <div className="col-xs-6 price">
              <span className="label label-primary">
                {toEuro(product.fields.price)}
              </span>
            </div>
            <div className="col-xs-6 text-right">
              <span className="label label-default">Weight: {product.fields.weight}</span>
              <br />
              <span className="label label-default">Stock: {product.fields.stocklevel}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

const intl = new Intl.NumberFormat('de-DE', { style: 'currency', 'currency': 'EUR' })
function toEuro(value) {
  return intl.format(value);
}
