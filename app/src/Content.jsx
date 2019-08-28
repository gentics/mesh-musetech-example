import React from 'react';
import { Route } from 'react-router-dom';
import WelcomeScreen from './WelcomeScreen';
import ProductList from './ProductList';
import ProductDetail from './ProductDetail';
import VideoList from './videos/VideoList';
import ScreenList from './screens/ScreenList';
import ScreenView from './screens/ScreenView';
import ExhibitionsList from './exhibitions/ExhibitionsList';
import ExhibitionView from './exhibitions/ExhibitionView';

export default function Content() {
  return (
    <>
      <Route path="/" exact component={WelcomeScreen} />
      <Route path="/category/:uuid" component={ProductList} />
      <Route path="/product/:uuid" component={ProductDetail} />
      <Route path="/videos/" component={VideoList} />
      <Route path="/screens/" exact component={ScreenList} />
      <Route path="/screens/:id" exact component={ScreenView} />
      <Route path="/exhibitions/" exact component={ExhibitionsList} />
      <Route path="/exhibitions/:id" exact component={ExhibitionView} />
    </>
  );
}
