from fabric.api import run, cd

def deploy():
    "Deploy the Rest api to production"
    with cd('JTime/JTime-rest'):
        run('git pull')
        run('npm install .')
        run('pm2 reload .')
