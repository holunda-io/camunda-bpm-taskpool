import { Action, ActionReducer, MetaReducer } from '@ngrx/store';
import { localStorageSync } from 'ngrx-store-localstorage';


export function storeLogger(reducer: ActionReducer<any>): ActionReducer<any> {
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

export function storePersist(reducer: ActionReducer<any>): ActionReducer<any> {
  return localStorageSync({
    keys: [
      {
        'user': [
          'currentUserId',
          'currentUserProfile'
        ]
      }
    ],
    rehydrate: true
  })(reducer);
}

export function metaReducers<T, V extends Action>(env): MetaReducer<any>[] {
  return env.production ? [
    storePersist
  ] : [storePersist, storeLogger];
}
