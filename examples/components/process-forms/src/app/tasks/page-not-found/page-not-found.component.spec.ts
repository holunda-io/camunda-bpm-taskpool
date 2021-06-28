import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import 'rxjs/add/observable/of';
import { PageNotFoundComponent } from './page-not-found.component';
import { FormsModule } from '@angular/forms';


describe('Component: TasklistComponent', () => {

  let component: PageNotFoundComponent;
  let fixture: ComponentFixture<PageNotFoundComponent>;

  beforeEach(waitForAsync(() => {

    TestBed.configureTestingModule({
      imports: [
        FormsModule
      ],
      declarations: [
        PageNotFoundComponent
      ],
      providers: [
      ],
    }).compileComponents().then(() => {
      // create component and test fixture
      fixture = TestBed.createComponent(PageNotFoundComponent);

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
