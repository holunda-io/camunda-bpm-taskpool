import {Action, MetaReducer} from '@ngrx/store';
import {storeFreeze} from 'ngrx-store-freeze';
import {localStorageSync} from 'ngrx-store-localstorage';

function storeLogger(reducer) {
  return (state, action: any): any => {
    const result = reducer(state, action);
    console.groupCollapsed(action.type);
    console.log('prev state', state);
    console.log('action', action);
    console.log('next state', result);
    console.groupEnd();

    return result;
  };
}

function storePersist<T, V extends Action>(): MetaReducer<T, V> {
  return localStorageSync({
    keys: [
      'user'
    ],
    rehydrate: true
  });
}

export function metaReducers<T, V extends Action>(env): MetaReducer<T, V>[] {
  return env.production ? [
    storePersist()
  ] : [
    storePersist(),
    storeLogger,
    storeFreeze
  ];
}
