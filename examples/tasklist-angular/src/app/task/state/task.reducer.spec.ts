import {LoadTasksAction, TasksLoadedAction} from './task.actions';
import {taskReducer, TaskState} from './task.reducer';
import {TaskWithDataEntries} from 'tasklist/models/task-with-data-entries';

describe('taskReducer', () => {

  const initialState: TaskState = {
    tasks: [],
    taskCount: 0,
    sortingColumn: null
  };

  it('should update tasks', () => {
    // given:
    const tasks: TaskWithDataEntries[] = [
      {
        dataEntries: [],
        task: {
          name: 'foo',
          businessKey: '',
          candidateGroups: [],
          candidateUsers: [],
          createTime: '',
          description: '',
          dueDate: '',
          id: '',
          url: '',
          processName: '',
        }
      }
    ];
    const action = new TasksLoadedAction(tasks);

    // when:
    const newState = taskReducer(initialState, action);

    // then:
    expect(newState.tasks).toBe(tasks);
  });

  it('ignores other actions', () => {
    // given:
    const action = new LoadTasksAction();

    // when:
    const newState = taskReducer(initialState, action);

    // then:
    expect(newState).toBe(initialState);
  });
});
