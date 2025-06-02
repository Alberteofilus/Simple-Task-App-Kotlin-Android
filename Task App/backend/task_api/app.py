from flask import Flask, request
from flask_cors import CORS
from flask_restful import Api, Resource
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime
import pytz
import pymysql

# ---------------- CONFIG ----------------
DB_USER = 'root'
DB_PASSWORD = ''
DB_HOST = 'localhost'
DB_NAME = 'taskdb'

SQLALCHEMY_DATABASE_URI = f"mysql+pymysql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}/{DB_NAME}"
SQLALCHEMY_TRACK_MODIFICATIONS = False

# -------------- APP INIT ---------------
app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = SQLALCHEMY_DATABASE_URI
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = SQLALCHEMY_TRACK_MODIFICATIONS

CORS(app)
api = Api(app)
db = SQLAlchemy(app)

# ----------- TIMEZONE FUNCTION ---------
def get_jakarta_time():
    return datetime.now(pytz.timezone("Asia/Jakarta"))

# ----------- MODEL ---------------------
class Task(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(100))
    description = db.Column(db.Text)
    category = db.Column(db.String(20))  # Important, Urgent, Regular
    status = db.Column(db.String(10), default='active')  # active, done, deleted
    updated_at = db.Column(db.DateTime, default=get_jakarta_time, onupdate=get_jakarta_time)

    def to_dict(self):
        return {
            "id": self.id,
            "title": self.title,
            "description": self.description,
            "category": self.category,
            "status": self.status,
            "updated_at": self.updated_at.strftime("%Y-%m-%d %H:%M:%S")
        }

# ----------- ROUTES --------------------
class TaskList(Resource):
    def get(self, category=None):
        try:
            if category and category.lower() != 'all':
                tasks = Task.query.filter(
                    Task.category == category.capitalize(),
                    Task.status != 'deleted'
                ).all()
            else:
                tasks = Task.query.filter(Task.status != 'deleted').all()
            
            counts = {
                'all': Task.query.filter(Task.status != 'deleted').count(),
                'important': Task.query.filter(Task.category == 'Important', Task.status != 'deleted').count(),
                'urgent': Task.query.filter(Task.category == 'Urgent', Task.status != 'deleted').count(),
                'regular': Task.query.filter(Task.category == 'Regular', Task.status != 'deleted').count()
            }
            
            return {
                'tasks': [task.to_dict() for task in tasks],
                'counts': counts
            }, 200
        except Exception as e:
            return {'error': str(e)}, 500

    def post(self):
        try:
            data = request.get_json()
            task = Task(
                title=data['title'],
                description=data['description'],
                category=data['category'].capitalize(),
                status='active'
            )
            db.session.add(task)
            db.session.commit()
            return task.to_dict(), 201
        except Exception as e:
            return {'error': str(e)}, 400

class TaskDetail(Resource):
    def get(self, task_id):
        task = Task.query.get(task_id)
        if not task or task.status == 'deleted':
            return {'message': 'Task not found'}, 404
        return task.to_dict(), 200

    def put(self, task_id):
        try:
            task = Task.query.get(task_id)
            if not task or task.status != 'active':
                return {'message': 'Task not found or not editable'}, 404
            
            data = request.get_json()
            task.title = data['title']
            task.description = data['description']
            task.category = data['category'].capitalize()
            task.updated_at = get_jakarta_time()
            db.session.commit()
            return task.to_dict(), 200
        except Exception as e:
            return {'error': str(e)}, 400

    def patch(self, task_id):
        try:
            task = Task.query.get(task_id)
            if not task:
                return {'message': 'Task not found'}, 404
            
            data = request.get_json()
            if 'status' in data and data['status'] in ['done', 'deleted']:
                task.status = data['status']
                task.updated_at = get_jakarta_time()
                db.session.commit()
            return task.to_dict(), 200
        except Exception as e:
            return {'error': str(e)}, 400

    def delete(self, task_id):
        try:
            task = Task.query.get(task_id)
            if not task:
                return {'message': 'Task not found'}, 404
            task.status = 'deleted'
            task.updated_at = get_jakarta_time()
            db.session.commit()
            return {'message': 'Task deleted'}, 200
        except Exception as e:
            return {'error': str(e)}, 400

# ---------- REGISTER RESOURCE ----------
api.add_resource(TaskList, '/tasks', '/tasks/<string:category>')
api.add_resource(TaskDetail, '/task/<int:task_id>')

# -------------- MAIN -------------------
if __name__ == '__main__':
    with app.app_context():
        db.create_all()
    app.run(debug=True)
