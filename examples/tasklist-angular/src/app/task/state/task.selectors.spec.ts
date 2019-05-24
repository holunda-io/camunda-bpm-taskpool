import {getTasks, StateWithTasks} from 'app/task/state/task.selectors';

describe('task selectors', () => {
  const state: StateWithTasks = {
    task: {
      sortingColumn: null,
      taskCount: 0,
      tasks: []
    }
  };

  it('should select current userId', () => {
    // when:
    const tasks = getTasks(state);

    // then:
    expect(tasks).toBe(state.task.tasks);
  });
});
