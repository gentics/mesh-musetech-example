import { useState, useEffect } from 'react';

export async function getNavigation() {
  return graphQl(`query Navigation {
    project {
      rootNode {
        children(filter: {schema: {is: category}}) {
          elements {
            uuid
            fields {
              ... on category {
                name
              }
            }
          }
        }
      }
    }
  }`);
}


export async function getExhibition(id, lang) {
  return graphQl(`
  query Exhibition($path: String) {
    node(path: $path) {
      uuid
      path
      fields {
        ... on Exhibition {
          id
          title
          description
          title_image(lang: "en") {
            path
          }
          public_number
          images {
            node(lang: "en") {
              path
            }
          }
          audio {
            path
          }
        }
      }
    }
  }
  `, { "path": "/exhibitions/" + id + ":" + lang });
}

export async function getScreen(id) {
  return graphQl(`
  query Screen($path: String) {
    node(path: $path) {
      uuid
      path
      version
      fields {
        ... on Screen {
          name
          id
          description
          location
          contents {
            type: __typename
            ... on ScreenEvent {
              title
              teaser
              start
              duration
              location
              image {
                node {
                  path
                }
              }
              video {
                node {
                  path
                }
              }
            }
            ... on ScreenExhibitionPromo {
              title
              teaser
              image {
                node {
                  path
                }
              }
              video {
                node {
                  path
                }
              }
            }
          }
        }
      }
    }
  }  
  `, { "path": "/screens/" + id });
}

export async function getExhibitions() {
  return graphQl(`
  {
    node(path: "/exhibitions") {
      children(lang: "de", perPage: 20000) {
        elements {
          uuid
          path
          fields {
            ... on Exhibition {
              id
              title
              title_image(lang: "en") {
                path
              }
              public_number
              images {
                node(lang: "en") {
                  path
                  fields {
                    ... on Image {
                      binary {
                        dominantColor
                      }
                    }
                  }
                }
              }
              audio {
                path
              }
            }
          }
        }
      }
    }
  }  
  `);
}

export async function getScreens() {
  return graphQl(`
  {
    node(path: "/screens") {
      children(lang: ["en", "de"]) {
        elements {
          uuid
          path
          fields {
            ... on Screen {
              name
              id
              description
              location
              contents {
                __typename
              }
            }
          }
        }
      }
    }
  }  
  `);
}

export async function getVideos() {
  return graphQl(`
  {
    node(path:"/videos") { 
      children {
        elements {
          uuid
          path
          fields {
            ... on Video {
              filename
              binary {
                fileSize
                mimeType
              }
            }
          }
        }
      }
    }
  }  
  `);

}

export async function getProducts(uuid) {
  return graphQl(`
  query Products($uuid:String) {
    node(uuid:$uuid) {
      fields {
        ... on category {
          name
          description
        }
      }
      children {
        elements {
          uuid
          fields {
            ... on vehicle {
              name
              weight
              description
              SKU
              price
              stocklevel
              vehicleImage {
                path
              }
            }
          }
        }
      }
    }
  }
  `, { uuid });
}

export function getProduct(uuid) {
  return graphQl(`
  query Product($uuid: String) {
    node(uuid: $uuid) {
      fields {
        ... on vehicle {
          name
          description
          SKU
          price
          weight
          stocklevel
          vehicleImage {
            path
          }
        }
      }
    }
  }
  `, { uuid });
}

export function getProject() {
  return get(`/demo`)
}

export function usePromise(promiseFn, changes) {
  const [state, setState] = useState();

  useEffect(() => {
    promiseFn().then(setState)
  }, changes)

  return state;
}

function graphQl(query, variables) {
  return post(`/demo/graphql`, { query, variables }).then(response => response.data);
}

function get(path) {
  return fetch(`/api/v1${path}`)
    .then(response => response.json());
}

function post(path, data) {
  return fetch(`/api/v1${path}`, {
    body: JSON.stringify(data),
    method: 'POST'
  }).then(response => response.json());
}