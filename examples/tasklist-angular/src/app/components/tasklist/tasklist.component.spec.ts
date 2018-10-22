import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { TaskHelperService } from 'app/services/task.helper.service';
import 'rxjs/add/observable/of';
import { TasklistComponent } from './tasklist.component';
import { Observable } from 'rxjs/Rx';

describe('Component: TasklistComponent', () => {

  let component: TasklistComponent;
  let fixture: ComponentFixture<TasklistComponent>;

  beforeEach(async(() => {

    TestBed.configureTestingModule({
      imports: [
      ],
      declarations: [
        TasklistComponent,
      ],
      providers: [
        { provide: TaskHelperService, useValue: { tasks: Observable.of([]) } },
      ],
    }).compileComponents().then(() => {
      // create component and test fixture
      fixture = TestBed.createComponent(TasklistComponent);

      // get test component from the fixture
      component = fixture.componentInstance;

      // detect changes
      fixture.detectChanges();
    });
  }));

  it('should create', () => {
    expect(component).toBeDefined();
  });

});
