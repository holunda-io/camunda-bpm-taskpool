import {LoadTasksAction, PageSelectedAction, TasksLoadedAction} from './task.actions';
import {taskReducer, TaskState} from './task.reducer';
import {TaskWithDataEntries} from 'tasklist/models/task-with-data-entries';

describe('taskReducer', () => {

  const initialState: TaskState = {
    tasks: [],
    taskCount: 0,
    sortingColumn: null,
    page: 0
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
    const action = new TasksLoadedAction({tasks: tasks, totalCount: 1});

    // when:
    const newState = taskReducer(initialState, action);

    // then:
    expect(newState.tasks).toBe(tasks);
  });

  it('should update page', () => {
    // given:
    const action = new PageSelectedAction(1);

    // when
    const newState = taskReducer(initialState, action);

    // then:
    expect(newState.page).toBe(1);
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
